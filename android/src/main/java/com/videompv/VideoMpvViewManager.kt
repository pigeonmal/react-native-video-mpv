package com.videompv

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.VideoMpvViewManagerInterface
import com.facebook.react.viewmanagers.VideoMpvViewManagerDelegate

@ReactModule(name = VideoMpvViewManager.NAME)
class VideoMpvViewManager : SimpleViewManager<VideoMpvView>(),
  VideoMpvViewManagerInterface<VideoMpvView> {
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

  @ReactProp(name = "color")
  override fun setColor(view: VideoMpvView?, color: String?) {
    view?.setBackgroundColor(Color.parseColor(color))
  }

  companion object {
    const val NAME = "VideoMpvView"
  }
}
