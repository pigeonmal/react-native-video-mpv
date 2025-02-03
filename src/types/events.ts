import type {
  OnBufferData,
  OnLoadData,
  OnPlaybackStateChangedData,
  OnProgressData,
  OnVideoErrorData,
  OnVideoStopData,
} from '../VideoMpvViewNativeComponent';

export interface ReactVideoMPVEvents {
  onBuffer?: (e: OnBufferData) => void;
  onError?: (e: OnVideoErrorData) => void;
  onLoad?: (e: OnLoadData) => void;
  onLoadStart?: () => void;
  onProgress?: (e: OnProgressData) => void;
  onPlaybackStateChanged?: (e: OnPlaybackStateChangedData) => void;
  onStop?: (e: OnVideoStopData) => void;
  onEndReached?: () => void;
}
