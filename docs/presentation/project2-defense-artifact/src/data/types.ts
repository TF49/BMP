export type SupportItem = {
  label?: string;
  title?: string;
  body?: string;
};

export type ChapterSupport = {
  label?: string;
  title?: string;
  copy?: string;
  items?: SupportItem[];
};

export type Metric = {
  label?: string;
  value?: string;
  note?: string;
};

export type ScenePos = {
  x?: number;
  y?: number;
  width?: number;
};

export type Chapter = {
  id: string;
  step?: string;
  title?: string;
  dotLabel?: string;
  claim?: string;
  sceneRole?: string;
  cameraIntent?: string;
  diagramNeed?: string;
  layout?: string;
  eyebrow?: string;
  body?: string[];
  metrics?: Metric[];
  support?: ChapterSupport;
  scenePos?: ScenePos;
};

export type CameraKeyframe = {
  p: number;
  camera: { x: number; y: number; scale: number };
  activeDot: number;
  visibility: Record<string, number>;
};

export type PresentationBlueprint = {
  title?: string;
  subtitle?: string;
  deckKicker?: string;
  theme?: string;
  chapterOrder: string[];
  chapters: Chapter[];
  keyframes?: CameraKeyframe[];
};
