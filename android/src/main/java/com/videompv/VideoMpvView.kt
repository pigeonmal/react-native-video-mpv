package com.videompv

import android.view.SurfaceHolder
import android.view.SurfaceView
import com.facebook.react.uimanager.ThemedReactContext

class VideoMpvView(context: ThemedReactContext) : SurfaceView(context), SurfaceHolder.Callback {

  companion object {
    internal const val TAG = "VideoMpvView"
  }
}
