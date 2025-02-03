import type { ViewProps } from 'react-native';
import type { ReactVideoMPVEvents } from './events';
import type { LangsPref, SubtitleStyle } from '../VideoMpvViewNativeComponent';

export type Headers = Record<string, string>;

export type EnumValues<T extends string | number> = T extends string
  ? `${T}` | T
  : T;

export type SideloadTrack = {
  title?: string;
  language?: string;
  uri: string;
};

export type ReactVideoMPVsourceProperties = {
  uri?: string;
  headers?: Headers;
  startPosition?: number;
  textTracks?: SideloadTrack[];
};

export type ReactVideoMPVSource = Readonly<
  Omit<ReactVideoMPVsourceProperties, 'uri'> & {
    uri?: string | NodeRequire;
  }
>;

export interface ReactVideoMPVProps extends ReactVideoMPVEvents, ViewProps {
  initialSource?: ReactVideoMPVSource;
  repeat?: boolean;
  zoomMode?: boolean;
  paused?: boolean;
  muted?: boolean;
  volume?: number; // 0-100, default 100
  textTrackDelay?: number;
  langsPref?: LangsPref;
  subStyle?: SubtitleStyle;
}
