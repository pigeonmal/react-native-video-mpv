import type { HostComponent, ViewProps } from 'react-native';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type {
  DirectEventHandler,
  Float,
  Int32,
  WithDefault,
} from 'react-native/Libraries/Types/CodegenTypes';

type Headers = ReadonlyArray<
  Readonly<{
    key: string;
    value: string;
  }>
>;

type SideloadTracks = ReadonlyArray<
  Readonly<{
    title?: string;
    language?: string;
    uri: string;
  }>
>;

export type VideoSrc = Readonly<{
  uri?: string;
  requestHeaders?: Headers;
  startPosition?: Float;
  textTracks?: SideloadTracks;
  minLoadRetryCount?: Int32; // Android
}>;

export type LangsPref = Readonly<{
  audio?: string; // default is os language
  sub?: string; // default is os language
  subMatchingAudio?: WithDefault<boolean, true>;
}>;

export type OnLoadData = Readonly<{
  currentTime: Float;
  duration: Float;
  naturalSize: Readonly<{
    width: Float;
    height: Float;
    orientation: 'landscape' | 'portrait' | 'square';
  }>;
  videoTracks: {
    id: Int32;
    selected: boolean;
    title?: string;
    language?: string;
    width: Int32;
    height: Int32;
  }[];
  audioTracks: {
    id: Int32;
    selected: boolean;
    title?: string;
    language?: string;
  }[];
  textTracks: {
    id: Int32;
    selected: boolean;
    title?: string;
    language?: string;
  }[];
}>;

export type OnVideoStopData = Readonly<{
  reason: Int32; // 0, 2, 3, 4, 5   Other are unknown
}>;

export type OnVideoErrorData = Readonly<{
  error: Readonly<{
    errorString: string;
    errorCode: Int32; // 1000 is mpv error
  }>;
}>;

export type OnBufferData = Readonly<{ isBuffering: boolean }>;

export type OnProgressData = Readonly<{
  currentTime: Float;
  seekableDuration: Float;
  progress: Float;
}>;

export type OnPlaybackStateChangedData = Readonly<{
  isPlaying: boolean;
  isSeeking: boolean;
}>;

type SubtitleStyle = Readonly<{
  fontSize?: WithDefault<Int32, 55>; // Default 55
  color?: WithDefault<string, '1.0/1.0/1.0'>; // Default white
  bold?: WithDefault<boolean, false>; // Default false
  backgroundColor?: WithDefault<string, '0.0/0.0/0.0/0.0'>; // Default to transparent black
}>;

export interface NativeProps extends ViewProps {
  src?: VideoSrc;
  repeat?: boolean;
  resizeMode?: string;
  paused?: boolean;
  muted?: boolean;
  volume?: Int32; // default 100
  textTrackDelay?: Float; // delai en seconds i think
  langsPref?: LangsPref;
  subStyle?: SubtitleStyle;

  onVideoLoad?: DirectEventHandler<OnLoadData>;
  onVideoLoadStart?: DirectEventHandler<{}>;
  onVideoBuffer?: DirectEventHandler<OnBufferData>;
  onVideoError?: DirectEventHandler<OnVideoErrorData>;
  onVideoProgress?: DirectEventHandler<OnProgressData>;
  onVideoStop?: DirectEventHandler<OnVideoStopData>;
  onVideoEndReached?: DirectEventHandler<{}>;
  onVideoPlaybackStateChanged?: DirectEventHandler<OnPlaybackStateChangedData>; // android only
}

export default codegenNativeComponent<NativeProps>(
  'VideoMpvView'
) as HostComponent<NativeProps>;
