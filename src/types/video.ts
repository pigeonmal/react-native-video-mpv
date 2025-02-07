import type { ViewProps } from 'react-native';
import type { ReactVideoMPVEvents } from './events';
import type { LangsPref } from '../VideoMpvViewNativeComponent';

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

export type SubtitleStyle = {
  fontSize?: number; // default 55
  color?: string; // default '1.0/1.0/1.0' (white)
  bold?: boolean; // default false
  backgroundColor?: string; // default '0.0/0.0/0.0/0.0' (transparent)
  borderStyle?: 'outline-and-shadow' | 'opaque-box' | 'background-box'; // default 'outline-and-shadow'
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
