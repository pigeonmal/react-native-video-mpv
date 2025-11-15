import type { HostComponent, ViewProps } from 'react-native';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type {
  DirectEventHandler,
  Double,
  Float,
  Int32,
  WithDefault,
} from 'react-native/Libraries/Types/CodegenTypes';
import codegenNativeCommands from 'react-native/Libraries/Utilities/codegenNativeCommands';

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
  subMatchingAudio?: boolean; // default true
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
  fontSize?: Int32;
  color?: string;
  bold?: boolean;
  backgroundColor?: string;
  borderStyle?: string;
}>;

interface NativeProps extends ViewProps {
  src?: VideoSrc;
  repeat?: boolean;
  zoomMode?: boolean;
  paused?: boolean;
  muted?: boolean;
  volume?: WithDefault<Int32, 100>; // default 100
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

interface NativeCommands {
  seek: (
    viewRef: React.ElementRef<HostComponent<NativeProps>>,
    time: Double
  ) => void;
  setPropString: (
    viewRef: React.ElementRef<HostComponent<NativeProps>>,
    propName: string,
    propValue: string
  ) => void;
  setPropInt: (
    viewRef: React.ElementRef<HostComponent<NativeProps>>,
    propName: string,
    propValue: Int32
  ) => void;
}

export const Commands: NativeCommands = codegenNativeCommands<NativeCommands>({
  supportedCommands: ['seek', 'setPropString', 'setPropInt'],
});
