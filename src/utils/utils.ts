import type {
  ReactVideoMPVSource,
  ReactVideoMPVsourceProperties,
} from '../types/video';
import { Image } from 'react-native';

type Source = ReactVideoMPVSource;

const convertToUri = (sourceItem: number): string | undefined => {
  const resolveItem = Image.resolveAssetSource(sourceItem);
  if (resolveItem) {
    return resolveItem.uri;
  } else {
    console.warn('cannot resolve item ', sourceItem);
    return undefined;
  }
};

export function resolveAssetSourceForVideo(
  source: Source
): ReactVideoMPVsourceProperties {
  // will convert source id to uri

  // This is deprecated, but we need to support it for backward compatibility
  if (typeof source === 'number') {
    return {
      uri: convertToUri(source),
    };
  }

  if ('uri' in source && typeof source.uri === 'number') {
    return {
      ...source,
      uri: convertToUri(source.uri),
    };
  }

  return source as ReactVideoMPVsourceProperties;
}

export function generateHeaderForNative(obj?: Record<string, any>) {
  if (!obj) {
    return [];
  }
  return Object.entries(obj).map(([key, value]) => ({ key, value }));
}
