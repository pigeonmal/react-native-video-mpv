import type { HostComponent, ViewProps } from 'react-native';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type {
  DirectEventHandler,
  Float,
  Int32,
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

export type OnVideoErrorData = Readonly<{
  error: Readonly<{
    errorString: string;
    errorCode: Int32;
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

/*
type SubtitleStyle = Readonly<{
  fontSize?: WithDefault<Int32, 0>; // Default to 0 = auto [0 | 20 | 18 | 16 | 12 | 6]
  color?: WithDefault<Int32, 0x00ffffff>; // Default 0x00ffffff = white
  bold?: WithDefault<boolean, false>; // Default false
  backgroundOpacity?: WithDefault<Int32, 0>; // Default 0, [0, 255]
  backgroundColor?: WithDefault<Int32, 0x00000000>; // Default to 0x00000000 = black
}>;
*/

export interface NativeProps extends ViewProps {
  src?: VideoSrc;
  repeat?: boolean;
  resizeMode?: string;
  selectedTextTrack?: Int32;
  selectedAudioTrack?: Int32;
  paused?: boolean;
  muted?: boolean;
  volume?: Int32; // default 100
  textTrackDelay?: Float; // delai en seconds i think

  onVideoLoad?: DirectEventHandler<OnLoadData>;
  onVideoLoadStart?: DirectEventHandler<{}>;
  onVideoBuffer?: DirectEventHandler<OnBufferData>;
  onVideoError?: DirectEventHandler<OnVideoErrorData>;
  onVideoProgress?: DirectEventHandler<OnProgressData>;
  onVideoEnd?: DirectEventHandler<{}>; // all
  onVideoPlaybackStateChanged?: DirectEventHandler<OnPlaybackStateChangedData>; // android only
}

export default codegenNativeComponent<NativeProps>(
  'VideoMpvView'
) as HostComponent<NativeProps>;
