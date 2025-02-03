import type { Component, ComponentClass, RefObject } from 'react';
import type {
  ReactVideoMPVSource,
  ReactVideoMPVsourceProperties,
} from '../types/video';
import { Image, findNodeHandle } from 'react-native';

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

export function getReactTag(
  ref: RefObject<
    | Component<unknown, unknown, unknown>
    | ComponentClass<unknown, unknown>
    | null
  >
): number {
  if (!ref.current) {
    throw new Error('VideoMPV Component is not mounted');
  }

  const reactTag = findNodeHandle(ref.current);

  if (!reactTag) {
    throw new Error(
      'Cannot find reactTag for VideoMPV Component in components tree'
    );
  }

  return reactTag;
}
