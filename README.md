# VideoMPV - React Native Video Player Component

A powerful React Native video player component built with MPV backend, providing advanced video playback capabilities with support for multiple audio and subtitle tracks, custom styling, and fine-grained playback control.

## Features

- **Multi-track Support**: Handle multiple video, audio, and subtitle tracks
- **Subtitle Customization**: Style subtitles with custom fonts, colors, backgrounds, and borders
- **Advanced Playback Control**: Seek, pause, mute, and adjust volume programmatically
- **Language Preferences**: Set preferred audio and subtitle languages with intelligent matching
- **Network Support**: Load videos from remote URLs with custom HTTP headers
- **Text Track Delays**: Adjust subtitle timing independently from video
- **Buffering Management**: Monitor and respond to buffering events
- **Error Handling**: Comprehensive error reporting with detailed error codes
- **Progress Tracking**: Real-time progress updates with seekable duration information
- **Playback State Management**: Track playing and seeking states

## Installation

```bash
npm install react-native-video-mpv
# or
yarn add react-native-video-mpv
```

## Basic Usage

```javascript
import { useRef } from 'react';
import { View } from 'react-native';
import VideoMPV from 'react-native-video-mpv';

export function VideoPlayer() {
  const videoRef = useRef(null);

  return (
    <VideoMPV
      ref={videoRef}
      initialSource={{
        uri: 'https://example.com/video.mp4',
      }}
      style={{ width: '100%', height: 300 }}
      onLoad={(data) => console.log('Video loaded', data)}
      onError={(error) => console.log('Error', error)}
    />
  );
}
```

## Props

### Video Source

#### `initialSource?: ReactVideoMPVSource`

Specifies the initial video source and playback options.

| Property | Type | Description |
|----------|------|-------------|
| `uri` | string | URL or local path to the video file |
| `headers` | object | Custom HTTP headers for network requests |
| `startPosition` | number | Initial playback position in seconds |
| `textTracks` | array | Array of subtitle/text track files to load |

**Example:**
```javascript
{
  uri: 'https://example.com/video.mp4',
  headers: {
    'Authorization': 'Bearer token123',
    'User-Agent': 'MyApp/1.0'
  },
  startPosition: 10.5,
  textTracks: [
    { uri: 'https://example.com/en.vtt', language: 'en', title: 'English' },
    { uri: 'https://example.com/fr.vtt', language: 'fr', title: 'Français' }
  ]
}
```

### Playback Control

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `paused` | boolean | false | Controls playback state (pause/resume) |
| `repeat` | boolean | false | Loop video playback |
| `muted` | boolean | false | Mute audio output |
| `volume` | number | 100 | Audio volume level (0-100) |
| `zoomMode` | boolean | false | Enable zoom mode for video display |

### Language & Localization

#### `langsPref?: LangsPref`

Configure language preferences for audio tracks and subtitles.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `audio` | string | OS language | Preferred audio language code (e.g., 'en', 'fr') |
| `sub` | string | OS language | Preferred subtitle language code |
| `subMatchingAudio` | boolean | true | Automatically use same language for subtitles as audio |

**Example:**
```javascript
{
  audio: 'fr',
  sub: 'en',
  subMatchingAudio: false
}
```

### Subtitle Styling

#### `subStyle?: SubtitleStyle`

Customize the appearance of subtitles.

| Property | Type | Description |
|----------|------|-------------|
| `fontSize` | number | Font size in pixels |
| `color` | string | Text color (hex format: '#RRGGBB') |
| `bold` | boolean | Enable bold text |
| `backgroundColor` | string | Background color (hex format) |
| `borderStyle` | string | Border style (e.g., 'solid', 'outline') |

**Example:**
```javascript
{
  fontSize: 16,
  color: '#FFFFFF',
  bold: true,
  backgroundColor: '#000000',
  borderStyle: 'solid'
}
```

### Timing

| Prop | Type | Description |
|------|------|-------------|
| `textTrackDelay` | number | Subtitle delay in seconds (positive or negative) |

### Style

| Prop | Type | Description |
|------|------|-------------|
| `style` | object | React Native style object for the component container |

## Events

### `onLoad`

Fired when video metadata is loaded.

**Data Structure:**
```typescript
{
  currentTime: number;
  duration: number;
  naturalSize: {
    width: number;
    height: number;
    orientation: 'landscape' | 'portrait' | 'square';
  };
  videoTracks: {
    id: number;
    selected: boolean;
    title?: string;
    language?: string;
    width: number;
    height: number;
  }[];
  audioTracks: {
    id: number;
    selected: boolean;
    title?: string;
    language?: string;
  }[];
  textTracks: {
    id: number;
    selected: boolean;
    title?: string;
    language?: string;
  }[];
}
```

### `onLoadStart`

Fired when video starts loading.

### `onBuffer`

Fired when buffering state changes.

### `onError`

Fired when an error occurs during playback.

### `onProgress`

Fired periodically during playback with current position information.

### `onStop`

Fired when video playback stops.

### `onEndReached`

Fired when the video reaches the end.

### `onPlaybackStateChanged`

Fired when playback state changes

```

## Ref Methods

Access the VideoMPV component methods through a ref to control playback programmatically.

### `seek(time: number)`

Seek to a specific time in seconds.

```javascript
const videoRef = useRef(null);

// Seek to 30 seconds
videoRef.current?.seek(30);
```

### `setSource(source?: ReactVideoMPVSource)`

Change the video source dynamically.

```javascript
videoRef.current?.setSource({
  uri: 'https://example.com/different-video.mp4'
});
```

### `setStringOption(option: string, value: string)`

Set a string option on the native player.

```javascript
videoRef.current?.setStringOption('subtitle-codepage', 'utf-8');
```

### `setIntOption(option: string, value: number)`

Set an integer option on the native player.

```javascript
videoRef.current?.setIntOption('video-scale', 2);
```

### `setAudioTrackID(id: number)`

Select an audio track by ID.

```javascript
// Use track ID from onLoad event
videoRef.current?.setAudioTrackID(1);

// Use 0 for auto selection
videoRef.current?.setAudioTrackID(0);

// Use -1 to disable audio
videoRef.current?.setAudioTrackID(-1);
```

### `setSubtitleTrackID(id: number)`

Select a subtitle track by ID.

```javascript
// Use track ID from onLoad event
videoRef.current?.setSubtitleTrackID(1);

// Use 0 for auto selection
videoRef.current?.setSubtitleTrackID(0);

// Use -1 to disable subtitles
videoRef.current?.setSubtitleTrackID(-1);
```


### Dynamic Subtitle Styling

```javascript
import { useRef, useState } from 'react';
import { View, Button, Slider } from 'react-native';
import VideoMPV from 'react-native-video-mpv';

export function SubtitleStyleCustomizer() {
  const videoRef = useRef(null);
  const [fontSize, setFontSize] = useState(16);
  const [color, setColor] = useState('#FFFFFF');

  const colors = [
    { label: 'White', value: '#FFFFFF' },
    { label: 'Yellow', value: '#FFFF00' },
    { label: 'Cyan', value: '#00FFFF' },
  ];

  return (
    <View style={{ flex: 1 }}>
      <VideoMPV
        ref={videoRef}
        initialSource={{
          uri: 'https://example.com/video.mp4',
        }}
        style={{ height: 300 }}
        subStyle={{
          fontSize: fontSize,
          color: color,
          bold: true,
          backgroundColor: '#000000AA',
          borderStyle: 'solid',
        }}
      />

      <View style={{ padding: 10, flex: 1 }}>
        <Text style={{ marginBottom: 10 }}>Font Size: {fontSize}</Text>
        <Slider
          style={{ height: 40, marginBottom: 20 }}
          minimumValue={10}
          maximumValue={32}
          value={fontSize}
          onValueChange={setFontSize}
        />

        <Text style={{ marginBottom: 10 }}>Text Color</Text>
        {colors.map((c) => (
          <Button
            key={c.value}
            title={`${c.label} ${color === c.value ? '✓' : ''}`}
            onPress={() => setColor(c.value)}
          />
        ))}
      </View>
    </View>
  );
}
```

## Platform Support

- **Only Android**: Full support with platform-specific features noted in documentation

## Error Codes

| Code | Description |
|------|-------------|
| 1000 | MPV playback engine error |

Check the `errorString` property for detailed error messages.

## TypeScript Support

The component is fully typed and provides type safety for all props, events, and ref methods.

```typescript
import type { VideoMPVRef, ReactVideoMPVProps } from 'react-native-video-mpv';

const videoRef = useRef<VideoMPVRef>(null);
```


## Contributing

Contributions are welcome! Please follow the standard GitHub pull request process.
