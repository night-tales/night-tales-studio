export type ProjectStatus = 'DRAFT' | 'PLANNING' | 'GENERATING' | 'READY' | 'RENDERING' | 'FAILED';

export interface StoryProject {
  id: string;
  title: string;
  prompt: string;
  status: ProjectStatus;
  scenes: Scene[];
}

export interface Scene {
  id: string;
  index: number;
  description: string;
  imagePrompt?: string;
  narrationText?: string;
  durationMs: number;
}

export interface GenerationJob {
  id: string;
  projectId: string;
  type: string;
  status: string;
}

export interface Blueprint {
  title: string;
  logline: string;
  genre: string;
  tone: string;
  sceneCount: number;
}

export interface Character {
  id: string;
  name: string;
  description: string;
  visualPrompt: string;
}

export type AssetKind = 'IMAGE' | 'AUDIO' | 'VIDEO' | 'SUBTITLE';

export interface MediaAsset {
  id: string;
  projectId: string;
  sceneId?: string;
  kind: AssetKind;
  uri: string;
  durationMs: number;
}

export type TrackType = 'VIDEO' | 'AUDIO' | 'SUBTITLE';

export interface TimelineClip {
  id: string;
  assetId: string;
  startMs: number;
  durationMs: number;
}

export interface TimelineTrack {
  id: string;
  type: TrackType;
  clips: TimelineClip[];
}

export interface Timeline {
  projectId: string;
  tracks: TimelineTrack[];
}
