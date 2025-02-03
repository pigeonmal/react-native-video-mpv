import type { ReactVideoMPVSource } from './video';

export interface VideoMPVRef {
  seek: (time: number) => void;
  setSource: (source: ReactVideoMPVSource | undefined) => void;
  setSubtitleTrackID: (id: number) => void;
  setAudioTrackID: (id: number) => void;
  setStringOption: (option: string, value: string) => void;
  setIntOption: (option: string, value: number) => void;
}
