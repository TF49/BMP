import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { CameraKeyframe, Chapter } from "@/data/types";
import {
  buildKeyframesFromScenes,
  progressForIndex,
  scenePos,
} from "./sceneLayout";
import { clamp, lerp, smooth } from "./math";

type CameraState = {
  x: number;
  y: number;
  scale: number;
  visibility: Record<string, number>;
};

export function usePresentationCamera(
  sceneNames: string[],
  chapterMap: Map<string, Chapter>,
  keyframesInput?: CameraKeyframe[],
) {
  const keyframes = useMemo(
    () =>
      keyframesInput?.length
        ? keyframesInput
        : buildKeyframesFromScenes(sceneNames, chapterMap),
    [keyframesInput, sceneNames, chapterMap],
  );

  const [scrollProgress, setScrollProgress] = useState(0);
  const [baseScale, setBaseScale] = useState(1);
  const [isMobile, setIsMobile] = useState(false);
  const [activeScene, setActiveScene] = useState(sceneNames[0]);
  const [activeDot, setActiveDot] = useState(0);

  const stateRef = useRef<CameraState>({
    x: 0,
    y: 0,
    scale: 1,
    visibility: Object.fromEntries(
      sceneNames.map((n, i) => [n, i === 0 ? 1 : 0]),
    ),
  });
  const targetRef = useRef({ ...stateRef.current });
  const wheelLocked = useRef(false);
  const activeSceneRef = useRef(activeScene);

  useEffect(() => {
    activeSceneRef.current = activeScene;
  }, [activeScene]);

  const pair = useCallback(() => {
    for (let i = 0; i < keyframes.length - 1; i += 1) {
      if (
        scrollProgress >= keyframes[i].p &&
        scrollProgress <= keyframes[i + 1].p
      ) {
        return [keyframes[i], keyframes[i + 1]] as const;
      }
    }
    const last = keyframes[keyframes.length - 1];
    return [last, last] as const;
  }, [keyframes, scrollProgress]);

  const [worldTransform, setWorldTransform] = useState("");
  const [sceneStyles, setSceneStyles] = useState<
    Record<string, { opacity: number; transform: string; interactive: boolean }>
  >({});

  useEffect(() => {
    let raf = 0;
    const tick = () => {
      const [start, end] = pair();
      const span = end.p - start.p || 1;
      const rawT = clamp((scrollProgress - start.p) / span, 0, 1);
      const t = smooth(rawT);
      const cameraEase = 0.08;
      const visibilityEase = 0.1;

      const target = targetRef.current;
      const state = stateRef.current;

      target.x = lerp(start.camera.x, end.camera.x, t);
      target.y = lerp(start.camera.y, end.camera.y, t);
      target.scale = lerp(start.camera.scale, end.camera.scale, t);
      sceneNames.forEach((name) => {
        target.visibility[name] = lerp(
          start.visibility[name] ?? 0,
          end.visibility[name] ?? 0,
          t,
        );
      });

      state.x = lerp(state.x, target.x, cameraEase);
      state.y = lerp(state.y, target.y, cameraEase);
      state.scale = lerp(state.scale, target.scale, cameraEase);

      const anchorY = 50;
      const nextSceneStyles: typeof sceneStyles = {};
      sceneNames.forEach((name) => {
        state.visibility[name] = lerp(
          state.visibility[name],
          target.visibility[name],
          visibilityEase,
        );
        const opacity = state.visibility[name];
        const lift = (1 - opacity) * 40;
        const sceneScale = 0.94 + opacity * 0.06;
        nextSceneStyles[name] = {
          opacity,
          transform: `translate3d(-50%, calc(-${anchorY}% + ${lift}px), 0) scale(${sceneScale})`,
          interactive: opacity > 0.5,
        };
      });

      const totalScale = state.scale * baseScale;
      setWorldTransform(
        `translate3d(-50%, -50%, 0) scale(${totalScale}) translate3d(${-state.x}px, ${-state.y}px, 0)`,
      );
      setSceneStyles(nextSceneStyles);

      const dot = rawT < 0.5 ? start.activeDot : end.activeDot;
      setActiveDot(dot);

      const best = sceneNames.reduce(
        (bestName, name) =>
          state.visibility[name] > state.visibility[bestName] ? name : bestName,
        sceneNames[0],
      );
      if (best !== activeSceneRef.current) {
        activeSceneRef.current = best;
        setActiveScene(best);
      }

      raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [scrollProgress, baseScale, pair, sceneNames]);

  const updateViewport = useCallback(() => {
    const mobile = window.innerWidth <= 820;
    setIsMobile(mobile);
    if (mobile) {
      setBaseScale(1);
    } else {
      const widthScale =
        window.innerWidth < 1500 ? window.innerWidth / 1500 : 1;
      const heightScale =
        window.innerHeight < 1080 ? window.innerHeight / 1080 : 1;
      const minScale =
        window.innerWidth < 1180 || window.innerHeight < 760 ? 0.68 : 0.76;
      setBaseScale(clamp(Math.min(widthScale, heightScale), minScale, 1));
    }
  }, []);

  useEffect(() => {
    updateViewport();
    window.addEventListener("resize", updateViewport);
    return () => window.removeEventListener("resize", updateViewport);
  }, [updateViewport]);

  const updateScrollProgress = useCallback(() => {
    const maxScroll = document.documentElement.scrollHeight - window.innerHeight;
    setScrollProgress(maxScroll <= 0 ? 0 : clamp(window.scrollY / maxScroll, 0, 1));
  }, []);

  useEffect(() => {
    const onScroll = () => updateScrollProgress();
    window.addEventListener("scroll", onScroll, { passive: true });
    updateScrollProgress();
    return () => window.removeEventListener("scroll", onScroll);
  }, [updateScrollProgress]);

  const scrollToProgress = useCallback((progress: number) => {
    const maxScroll = document.documentElement.scrollHeight - window.innerHeight;
    window.scrollTo({
      top: clamp(progress, 0, 1) * maxScroll,
      behavior: "smooth",
    });
  }, []);

  const scrollToScene = useCallback(
    (name: string) => {
      const index = sceneNames.indexOf(name);
      if (index < 0) return;
      scrollToProgress(progressForIndex(index, sceneNames.length));
    },
    [sceneNames, scrollToProgress],
  );

  const scrollByChapter = useCallback(
    (direction: number) => {
      const idx = sceneNames.indexOf(activeSceneRef.current);
      const next = clamp(idx + direction, 0, sceneNames.length - 1);
      scrollToProgress(progressForIndex(next, sceneNames.length));
    },
    [sceneNames, scrollToProgress],
  );

  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      const tag = (event.target as HTMLElement)?.tagName?.toLowerCase() ?? "";
      if (tag === "input" || tag === "textarea" || tag === "select") return;
      if (
        event.key === " " ||
        event.code === "Space" ||
        event.key === "ArrowDown" ||
        event.key === "PageDown"
      ) {
        event.preventDefault();
        scrollByChapter(1);
        return;
      }
      if (event.key === "ArrowUp" || event.key === "PageUp") {
        event.preventDefault();
        scrollByChapter(-1);
      }
      if (event.key === "Home") {
        event.preventDefault();
        scrollToScene(sceneNames[0]);
      }
      if (event.key === "End") {
        event.preventDefault();
        scrollToScene(sceneNames[sceneNames.length - 1]);
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [sceneNames, scrollByChapter, scrollToScene]);

  useEffect(() => {
    const onWheel = (event: WheelEvent) => {
      if (wheelLocked.current) {
        event.preventDefault();
        return;
      }
      if (Math.abs(event.deltaY) < 10) return;
      event.preventDefault();
      wheelLocked.current = true;
      window.setTimeout(() => {
        wheelLocked.current = false;
      }, 720);
      scrollByChapter(event.deltaY > 0 ? 1 : -1);
    };
    window.addEventListener("wheel", onWheel, { passive: false });
    return () => window.removeEventListener("wheel", onWheel);
  }, [scrollByChapter]);

  const getSceneLayoutStyle = useCallback(
    (chapterId: string, index: number) => {
      const chapter = chapterMap.get(chapterId);
      if (!chapter) return {};
      const p = scenePos(chapter, index);
      return {
        left: `${p.x || 0}px`,
        top: `${p.y || 0}px`,
        width: `min(${p.width || 1280}px, min(90vw, calc(100vw - 140px)))`,
      };
    },
    [chapterMap],
  );

  return {
    worldTransform,
    sceneStyles,
    activeScene,
    activeDot,
    isMobile,
    scrollToScene,
    scrollByChapter,
    getSceneLayoutStyle,
  };
}
