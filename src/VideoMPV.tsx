import {
  forwardRef,
  useCallback,
  useImperativeHandle,
  useRef,
  useState,
  type ElementRef,
} from 'react';
import {
  generateHeaderForNative,
  getReactTag,
  resolveAssetSourceForVideo,
} from './utils/utils';
import { UIManager, View, StyleSheet } from 'react-native';
import type { NativeSyntheticEvent } from 'react-native';
import type { ReactVideoMPVProps, ReactVideoMPVSource } from './types/video';
import type {
  OnBufferData,
  OnLoadData,
  OnPlaybackStateChangedData,
  OnProgressData,
  OnVideoErrorData,
  OnVideoStopData,
  VideoSrc,
} from './VideoMpvViewNativeComponent';
import type { VideoMPVRef } from './types/video-ref';
import VideoMpvViewNativeComponent from './VideoMpvViewNativeComponent';

const getNativeSource = (
  source?: ReactVideoMPVSource
): VideoSrc | undefined => {
  if (!source) return undefined;

  const resolvedSource = resolveAssetSourceForVideo(source);
  let uri = resolvedSource.uri || '';
  if (uri && uri.match(/^\//)) {
    uri = `file://${uri}`;
  }
  if (!uri) {
    console.log('Trying to load empty source');
  }

  return {
    uri,
    requestHeaders: generateHeaderForNative(resolvedSource.headers),
    startPosition: resolvedSource.startPosition,
    textTracks: resolvedSource.textTracks,
  };
};

const VideoMPV = forwardRef<VideoMPVRef, ReactVideoMPVProps>(
  (
    {
      initialSource,
      muted,
      repeat,
      zoomMode,
      paused,
      volume,
      langsPref,
      subStyle,
      textTrackDelay,
      onBuffer,
      onEndReached,
      onStop,
      onError,
      onLoad,
      onLoadStart,
      onPlaybackStateChanged,
      onProgress,
      style,
      ...rest
    },
    ref
  ) => {
    const nativeRef =
      useRef<ElementRef<typeof VideoMpvViewNativeComponent>>(null);
    const [nativeSource, setNativeSource] = useState<VideoSrc | undefined>(
      getNativeSource(initialSource)
    );

    const onVideoError = useCallback(
      (e: NativeSyntheticEvent<OnVideoErrorData>) => {
        onError?.(e.nativeEvent);
      },
      [onError]
    );

    const onVideoProgress = useCallback(
      (e: NativeSyntheticEvent<OnProgressData>) => {
        onProgress?.(e.nativeEvent);
      },
      [onProgress]
    );

    const onVideoBuffer = useCallback(
      (e: NativeSyntheticEvent<OnBufferData>) => {
        onBuffer?.(e.nativeEvent);
      },
      [onBuffer]
    );

    const onVideoLoad = useCallback(
      (e: NativeSyntheticEvent<OnLoadData>) => {
        onLoad?.(e.nativeEvent);
      },
      [onLoad]
    );

    const onVideoStop = useCallback(
      (e: NativeSyntheticEvent<OnVideoStopData>) => {
        onStop?.(e.nativeEvent);
      },
      [onStop]
    );

    const onVideoPlaybackStateChanged = useCallback(
      (e: NativeSyntheticEvent<OnPlaybackStateChangedData>) => {
        onPlaybackStateChanged?.(e.nativeEvent);
      },
      [onPlaybackStateChanged]
    );

    const sendCommand = useCallback((command: string, args: any[]) => {
      UIManager.dispatchViewManagerCommand(
        getReactTag(nativeRef),
        command,
        args
      );
    }, []);

    const seek = useCallback(
      (time: number) => {
        if (time == null || isNaN(time)) {
          throw new Error('Invalid time');
        }
        let wantedTime = time;
        if (wantedTime < 0) wantedTime = 0;
        sendCommand('seek', [wantedTime]);
      },
      [sendCommand]
    );

    const setStringOption = useCallback(
      (option: string, value: string) => {
        sendCommand('setPropString', [option, value]);
      },
      [sendCommand]
    );

    const setIntOption = useCallback(
      (option: string, value: number) => {
        sendCommand('setPropInt', [option, value]);
      },
      [sendCommand]
    );

    const setSource = useCallback((source?: ReactVideoMPVSource) => {
      setNativeSource(getNativeSource(source));
    }, []);

    const setTrackID = useCallback(
      (prop: 'sid' | 'aid', id: number) => {
        if (id > 0) {
          setIntOption(prop, id);
        } else {
          setStringOption(prop, id === 0 ? 'auto' : 'no');
        }
      },
      [setStringOption, setIntOption]
    );

    const setSubtitleTrackID = useCallback(
      (id: number) => setTrackID('sid', id),
      [setTrackID]
    );
    const setAudioTrackID = useCallback(
      (id: number) => setTrackID('aid', id),
      [setTrackID]
    );

    useImperativeHandle(
      ref,
      () => ({
        seek,
        setSource,
        setStringOption,
        setIntOption,
        setSubtitleTrackID,
        setAudioTrackID,
      }),
      [
        seek,
        setSource,
        setStringOption,
        setIntOption,
        setSubtitleTrackID,
        setAudioTrackID,
      ]
    );

    return (
      <View style={style} {...rest}>
        <VideoMpvViewNativeComponent
          ref={nativeRef}
          style={StyleSheet.absoluteFillObject}
          src={nativeSource}
          muted={muted}
          zoomMode={zoomMode}
          subStyle={subStyle}
          langsPref={langsPref}
          paused={paused}
          repeat={repeat}
          volume={volume}
          textTrackDelay={textTrackDelay}
          onVideoEndReached={onEndReached}
          onVideoLoadStart={onLoadStart}
          onVideoStop={onStop ? onVideoStop : undefined}
          onVideoError={onError ? onVideoError : undefined}
          onVideoProgress={onProgress ? onVideoProgress : undefined}
          onVideoBuffer={onBuffer ? onVideoBuffer : undefined}
          onVideoLoad={onLoad ? onVideoLoad : undefined}
          onVideoPlaybackStateChanged={
            onPlaybackStateChanged ? onVideoPlaybackStateChanged : undefined
          }
        />
      </View>
    );
  }
);
VideoMPV.displayName = 'VideoMPV';
export default VideoMPV;
