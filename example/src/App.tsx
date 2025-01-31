import { useState } from 'react';
import { View, StyleSheet, Pressable, Text } from 'react-native';
import type { VideoSrc } from '../../src/VideoMpvViewNativeComponent';
import { VideoMpvView } from 'react-native-video-mpv';

const testSource: VideoSrc = {
  startPosition: 10,
  textTracks: [
    {
      // Will work
      uri: 'https://subs5.strem.io/en/download/subencoding-stremio-utf8/src-api/file/1958347503',
      language: 'fr',
    },
    {
      // Don't work
      uri: 'https://subs5.strem.io/en/download/subencoding-stremio-utf8/src-api/file',
      language: 'jp',
      title: 'TEST',
    },
    {
      // Will work
      uri: 'https://subs5.strem.io/en/download/subencoding-stremio-utf8/src-api/file/1958334424',
      language: 'hp',
      title: 'TEST 2',
    },
    {
      // Will work
      uri: 'https://subs5.strem.io/en/download/subencoding-stremio-utf8/src-api/file/1958334424',
      language: 'h3',
      title: 'TEST3',
    },
    {
      // Will not work
      uri: 'https://subs5.strem.io/en/download/subencoding-stremio-utf8/src-api/file/',
      language: 'h4',
      title: 'TEST 4',
    },
  ],
  uri: 'https://github.com/ietf-wg-cellar/matroska-test-files/raw/refs/heads/master/test_files/test5.mkv',
};

const testSource2: VideoSrc = {
  startPosition: 100,
  uri: 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
};

export default function App() {
  const [paused, setPaused] = useState(false);
  const [source, setSource] = useState<VideoSrc | undefined>(testSource);

  return (
    <View style={styles.container}>
      <VideoMpvView
        src={source}
        style={styles.box}
        repeat
        resizeMode="none"
        selectedTextTrack={9}
        paused={paused}
        onVideoBuffer={() => console.log('onVideoBuffer')}
        onVideoEnd={() => console.log('onVideoEnd')}
        onVideoLoad={(event) => console.log('onVideoLoad', event)}
        onVideoError={(event) => console.log('OnVideoError', event)}
        onVideoLoadStart={() => console.log('Load started')}
        onVideoPlaybackStateChanged={(event) =>
          console.log('Playback changed', event)
        }
        onVideoProgress={(event) => console.log('Progress', event)}
      />
      <View style={styles.parer}>
        <Pressable
          style={styles.button}
          onPress={() => {
            setSource(undefined);
          }}
        >
          <Text style={styles.textButton}>Source undefinded</Text>
        </Pressable>
        <Pressable
          style={styles.button}
          onPress={() => {
            setSource(testSource);
          }}
        >
          <Text style={styles.textButton}>Source 1</Text>
        </Pressable>
        <Pressable
          style={styles.button}
          onPress={() => {
            setSource(testSource2);
          }}
        >
          <Text style={styles.textButton}>Source 2</Text>
        </Pressable>
      </View>
      <View style={styles.parer}>
        <Pressable
          style={styles.button}
          onPress={() => {
            setPaused(!paused);
          }}
        >
          <Text style={styles.textButton}>PLAY / PAUSE</Text>
        </Pressable>
        <Pressable style={styles.button} onPress={() => {}}>
          <Text style={styles.textButton}>SEEK</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: '100%',
    backgroundColor: 'red',
    height: 300,
  },
  button: {
    width: 100,
    margin: 30,
    height: 50,
    backgroundColor: 'black',
  },
  textButton: {
    color: 'white',
  },
  parer: {
    flexDirection: 'row',
  },
});
