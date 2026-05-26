import raw from "../../../project2-blueprint.json";
import type { PresentationBlueprint } from "./types";

export const presentationBlueprint = raw as PresentationBlueprint;

export function getChapterMap(blueprint: PresentationBlueprint) {
  return new Map(blueprint.chapters.map((c) => [c.id, c]));
}
