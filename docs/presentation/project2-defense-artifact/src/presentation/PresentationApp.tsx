import { useMemo } from "react";
import { presentationBlueprint, getChapterMap } from "@/data/blueprint";
import { Aurora } from "@/components/react-bits/Aurora";
import { usePresentationCamera } from "./camera/usePresentationCamera";
import { BrandDock } from "./chrome/BrandDock";
import { ChapterDock } from "./chrome/ChapterDock";
import { ProgressRail } from "./chrome/ProgressRail";
import { ChapterScene } from "./scenes/ChapterScene";

const themeClass: Record<string, string> = {
  "engineering-systems": "theme-engineering-systems",
  "product-narrative": "",
};

export function PresentationApp() {
  const blueprint = presentationBlueprint;
  const sceneNames = blueprint.chapterOrder;
  const chapterMap = useMemo(() => getChapterMap(blueprint), [blueprint]);
  const orderedChapters = useMemo(
    () => sceneNames.map((id) => chapterMap.get(id)!),
    [sceneNames, chapterMap],
  );

  const camera = usePresentationCamera(
    sceneNames,
    chapterMap,
    blueprint.keyframes,
  );

  const activeChapter = chapterMap.get(camera.activeScene);

  return (
    <div
      className={`min-h-screen ${themeClass[blueprint.theme ?? ""] ?? ""}`}
      data-active-scene={camera.activeScene}
      style={{
        background: `
          radial-gradient(circle at top left, rgba(123, 202, 247, 0.16), transparent 26%),
          radial-gradient(circle at 82% 18%, rgba(34, 104, 246, 0.15), transparent 22%),
          radial-gradient(circle at 72% 78%, rgba(241, 137, 98, 0.14), transparent 18%),
          linear-gradient(180deg, var(--pres-bg-soft) 0%, var(--pres-bg) 100%)`,
      }}
    >
      <Aurora />
      <div
        className="pointer-events-none fixed inset-0 z-0 opacity-45"
        style={{
          backgroundImage: `
            linear-gradient(rgba(23, 25, 31, 0.05) 1px, transparent 1px),
            linear-gradient(90deg, rgba(23, 25, 31, 0.05) 1px, transparent 1px)`,
          backgroundSize: "48px 48px",
        }}
        aria-hidden
      />

      <BrandDock
        kicker={blueprint.deckKicker}
        title={blueprint.title}
        subtitle={blueprint.subtitle}
      />
      <ChapterDock
        step={activeChapter?.step}
        title={activeChapter?.title}
        claim={activeChapter?.claim}
      />
      <ProgressRail
        chapters={orderedChapters}
        activeDot={camera.activeDot}
        onSelect={camera.scrollToScene}
      />

      <div className="relative z-10 h-[1000vh]" aria-hidden />

      <div className="pointer-events-none fixed inset-0 z-20 flex items-center justify-center overflow-hidden">
        <div
          id="world"
          className="absolute left-1/2 top-1/2 will-change-transform"
          style={{ transform: camera.worldTransform }}
        >
          {orderedChapters.map((chapter, index) => {
            const style = camera.sceneStyles[chapter.id];
            const layout = camera.getSceneLayoutStyle(chapter.id, index);
            const isActive = camera.activeScene === chapter.id;
            return (
              <section
                key={chapter.id}
                id={chapter.id}
                className="pointer-events-none absolute -translate-x-1/2 -translate-y-1/2"
                style={{
                  ...layout,
                  opacity: style?.opacity ?? 0,
                  transform: style?.transform,
                }}
              >
                <div
                  className={
                    style?.interactive ?
                      "pointer-events-auto"
                    : "pointer-events-none"
                  }
                >
                  <ChapterScene
                    chapter={chapter}
                    isActive={isActive}
                    onGoOpening={() => camera.scrollToScene(sceneNames[0])}
                  />
                </div>
              </section>
            );
          })}
        </div>
      </div>
    </div>
  );
}
