import { View, StyleSheet } from 'react-native';
import { VideoMpvView } from 'react-native-video-mpv';

export default function App() {
  return (
    <View style={styles.container}>
      <VideoMpvView
        src={{
          uri: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
        }}
        style={styles.box}
      />
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
    height: 300,
  },
});
