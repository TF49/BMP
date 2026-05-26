import type { Chapter, CameraKeyframe } from "@/data/types";

export function scenePos(chapter: Chapter, index: number) {
  if (chapter.scenePos) return chapter.scenePos;
  return {
    x: chapter.cameraIntent === "compare" ? 1480 * (index % 2) : 0,
    y: 360 + index * 1120,
    width: chapter.layout === "closing" ? 1120 : 1280,
  };
}

export function buildKeyframesFromScenes(
  sceneNames: string[],
  chapterMap: Map<string, Chapter>,
): CameraKeyframe[] {
  const n = sceneNames.length;
  return sceneNames.map((name, index) => {
    const chapter = chapterMap.get(name)!;
    const p = scenePos(chapter, index);
    const visibility = Object.fromEntries(
      sceneNames.map((sceneName) => [sceneName, sceneName === name ? 1 : 0]),
    );
    if (index > 0) visibility[sceneNames[index - 1]] = 0.08;
    const scale = index === 0 || index === n - 1 ? 1.02 : 0.98;
    return {
      p: n === 1 ? 0 : index / (n - 1),
      camera: { x: p.x || 0, y: p.y || 0, scale },
      activeDot: index,
      visibility,
    };
  });
}

export function progressForIndex(index: number, total: number) {
  return total === 1 ? 0 : index / (total - 1);
}
