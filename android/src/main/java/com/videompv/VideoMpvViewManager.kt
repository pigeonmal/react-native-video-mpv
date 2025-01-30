package com.videompv

import android.util.Log
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.VideoMpvViewManagerDelegate
import com.facebook.react.viewmanagers.VideoMpvViewManagerInterface
import com.videompv.api.VideoSrc
import com.videompv.toolbox.ReactBridgeUtils.safeGetDoubleFromArray

@ReactModule(name = VideoMpvViewManager.NAME)
class VideoMpvViewManager :
        SimpleViewManager<VideoMpvView>(), VideoMpvViewManagerInterface<VideoMpvView> {
  private val mDelegate: ViewManagerDelegate<VideoMpvView>

  init {
    mDelegate = VideoMpvViewManagerDelegate(this)
  }

  override fun getDelegate(): ViewManagerDelegate<VideoMpvView>? {
    return mDelegate
  }

  override fun getName(): String {
    return NAME
  }

  public override fun createViewInstance(context: ThemedReactContext): VideoMpvView {
    return VideoMpvView(context)
  }

  override fun onDropViewInstance(view: VideoMpvView) {
    view.destroy()
  }
  override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> = EventTypes.toMap()

  override fun addEventEmitters(reactContext: ThemedReactContext, view: VideoMpvView) {
    super.addEventEmitters(reactContext, view)
    view.eventEmitter.addEventEmitters(reactContext, view)
  }

  @ReactProp(name = PROP_SRC)
  override fun setSrc(videoView: VideoMpvView, src: ReadableMap?) {
    val context = videoView.context.applicationContext
    videoView.setSource(VideoSrc.parse(src, context))
  }

  @ReactProp(name = PROP_REPEAT, defaultBoolean = false)
  override fun setRepeat(videoView: VideoMpvView, repeat: Boolean) {
    videoView.setRepeatModifier(repeat)
  }

  @ReactProp(name = PROP_RESIZE_MODE)
  override fun setResizeMode(videoView: VideoMpvView, resizeMode: String?) {
    videoView.setResizeMode(resizeMode)
  }

  @ReactProp(name = PROP_SELECTED_TEXT_TRACK, defaultInt = -1)
  override fun setSelectedTextTrack(videoView: VideoMpvView, selectedTextTrack: Int) {
    videoView.setTextIdTrack(selectedTextTrack)
  }

  @ReactProp(name = PROP_SELECTED_AUDIO_TRACK, defaultInt = -1)
  override fun setSelectedAudioTrack(videoView: VideoMpvView, selectedAudioTrack: Int) {
    videoView.setAudioIdTrack(selectedAudioTrack)
  }

  @ReactProp(name = PROP_PAUSED, defaultBoolean = false)
  override fun setPaused(videoView: VideoMpvView, paused: Boolean) {
    videoView.setPausedModifier(paused)
  }

  @ReactProp(name = PROP_MUTED, defaultBoolean = false)
  override fun setMuted(videoView: VideoMpvView, muted: Boolean) {
    videoView.setMutedModifier(muted)
  }

  @ReactProp(name = PROP_VOLUME, defaultInt = 100)
  override fun setVolume(videoView: VideoMpvView, volume: Int) {
    videoView.setVolumeModifier(volume)
  }

  @ReactProp(name = PROP_TEXT_TRACK_DELAY, defaultFloat = 0f)
  override fun setTextTrackDelay(videoView: VideoMpvView, textTrackDelay: Float) {
    videoView.setTextTrackDelay(textTrackDelay.toDouble())
  }

  override fun receiveCommand(videoView: VideoMpvView, commandId: String, args: ReadableArray?) {
    super.receiveCommand(videoView, commandId, args)
    when (commandId) {
      COMMAND_SEEK_NAME -> {
        val seekTo = safeGetDoubleFromArray(args, 0, null)
        if (seekTo != null) {
          videoView.seek(seekTo)
        }
      }
    }
  }

  companion object {
    const val NAME = "VideoMpvView"

    private const val PROP_SRC = "src"
    private const val PROP_REPEAT = "repeat"
    private const val PROP_RESIZE_MODE = "resizeMode"
    private const val PROP_SELECTED_TEXT_TRACK = "selectedTextTrack"
    private const val PROP_SELECTED_AUDIO_TRACK = "selectedAudioTrack"
    private const val PROP_PAUSED = "paused"
    private const val PROP_MUTED = "muted"
    private const val PROP_VOLUME = "volume"
    private const val PROP_TEXT_TRACK_DELAY = "textTrackDelay"

    private const val COMMAND_SEEK_NAME = "seek"
  }
}
