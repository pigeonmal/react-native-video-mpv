package com.videompv

import android.os.Build
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext
import com.videompv.MPVUtil.MPVEventId
import com.videompv.MPVUtil.MPVFormat
import com.videompv.api.BasicTrack
import com.videompv.api.VideoBasicTrack
import com.videompv.api.VideoSrc
import dev.jdtech.mpv.MPVLib
import java.util.Locale

class VideoMpvView(context: ThemedReactContext) :
        SurfaceView(context), SurfaceHolder.Callback, LifecycleEventListener, MPVLib.EventObserver {

  /* SYSTEM */
  internal final val eventEmitter = VideoMpvEventEmitter()
  private var activityIsForeground = true
  private var playerDestoryed = false
  private var filePath: String? = null
  private var hwdec: String = "auto" // https://mpv.io/manual/stable/#options-hwdec
  private var voInUse: String = "gpu" // https://mpv.io/manual/stable/#video-output-drivers-vo

  /* PROPS */
  private var src: VideoSrc = VideoSrc()
  private var paused: Boolean = false
  private var muted: Boolean = false
  private var volume = 100
  private var repeat: Boolean = true // Default true by mpv but false in my library
  private var selectedTextTrack: Int = -1
  private var selectedAudioTrack: Int = -1
  // private var scaleType: String = ScaleType.SURFACE_BEST_FIT
  private var spuDelay: Double = 0.0

  init {
    MPVLib.create(context)
    initOptions()

    MPVLib.init()

    /* set hardcoded options */
    postInitOptions()
    // would crash before the surface is attached
    MPVLib.setOptionString("force-window", "no")
    // need to idle at least once for playFile() logic to work
    MPVLib.setOptionString("idle", "yes")

    holder.addCallback(this)
    observeProperties()
    MPVLib.addObserver(this)
  }

  fun initOptions() {
    // low device (phone) optimized profile
    MPVLib.setOptionString("profile", "fast")
    MPVLib.setOptionString("vo", voInUse)

    // vo: set display fps as reported by android
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val disp = ContextCompat.getDisplayOrDefault(context)
      val refreshRate = disp.mode.refreshRate

      Log.v(TAG, "Display ${disp.displayId} reports FPS of $refreshRate")
      MPVLib.setOptionString("display-fps-override", refreshRate.toString())
    } else {
      Log.v(
              TAG,
              "Android version too old, disabling refresh rate functionality " +
                      "(${Build.VERSION.SDK_INT} < ${Build.VERSION_CODES.M})"
      )
    }

    val deviceLanguage = Locale.getDefault().isO3Language
    Log.d(TAG, "Device language: $deviceLanguage")
    MPVLib.setOptionString("alang", deviceLanguage)
    MPVLib.setOptionString("slang", deviceLanguage)

    // MPVLib.setOptionString("deband", "yes")
    MPVLib.setOptionString("video-sync", "audio")
    MPVLib.setOptionString("gpu-context", "android")
    MPVLib.setOptionString("opengl-es", "yes")
    MPVLib.setOptionString("hwdec", hwdec)
    MPVLib.setOptionString("hwdec-codecs", "h264,hevc,mpeg4,mpeg2video,vp8,vp9,av1")
    MPVLib.setOptionString("ao", "audiotrack,opensles")
    // MPVLib.setOptionString("tls-verify", "yes")
    // MPVLib.setOptionString("tls-ca-file", "${this.context.filesDir.path}/cacert.pem")
    MPVLib.setOptionString("input-default-bindings", "no")
    MPVLib.setOptionString("input-builtin-bindings", "no")
    MPVLib.setOptionString("input-builtin-dragging", "no")

    val cacheMegs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) 64 else 32
    MPVLib.setOptionString("demuxer-max-bytes", "${cacheMegs * 1024 * 1024}")
    MPVLib.setOptionString("demuxer-max-back-bytes", "${cacheMegs * 1024 * 1024}")

    // workaround for <https://github.com/mpv-player/mpv/issues/14651>
    MPVLib.setOptionString("vd-lavc-film-grain", "cpu")
  }

  fun postInitOptions() {
    MPVLib.setOptionString("save-position-on-quit", "no")
  }

  /**
   * Deinitialize libmpv.
   *
   * Call this once before the view is destroyed.
   */
  fun destroy() {
    playerDestoryed = true
    MPVLib.removeObserver(this)

    // Disable surface callbacks to avoid using unintialized mpv state
    holder.removeCallback(this)

    MPVLib.destroy()
  }

  fun observeProperties() {
    MPVLib.observeProperty("eof-reached", MPVFormat.MPV_FORMAT_NONE)
    MPVLib.observeProperty("core-idle", MPVFormat.MPV_FORMAT_FLAG) // Like "pause"
    MPVLib.observeProperty("pause", MPVFormat.MPV_FORMAT_FLAG)
    MPVLib.observeProperty("time-pos", MPVFormat.MPV_FORMAT_INT64)
  }

  /* Events */

  override fun eventProperty(property: String) {}

  override fun eventProperty(property: String, value: Long) {
    when (property) {
      "time-pos" -> updatePlaybackPos(value)
    }
  }

  override fun eventProperty(property: String, value: Boolean) {}

  override fun eventProperty(property: String, value: String) {}

  override fun eventProperty(property: String, value: Double) {}

  override fun event(eventId: Int) {
    when (eventId) {
      MPVEventId.MPV_EVENT_FILE_LOADED -> onVideoLoaded()
    }
  }

  fun updatePlaybackPos(pos: Long) {
    if (playerDestoryed) return
    Log.d(TAG, "PROGRESS $pos")
    eventEmitter.onVideoProgress(
            pos.toDouble(),
            MPVLib.getPropertyDouble("duration"),
            MPVLib.getPropertyDouble("percent-pos") / 100.0
    )
  }

  private fun onVideoLoaded() {
    Log.d(TAG, "Video loaded !")

    val textTracks = ArrayList<BasicTrack>()
    val audioTracks = ArrayList<BasicTrack>()
    val videosTracks = ArrayList<VideoBasicTrack>()

    val count = MPVLib.getPropertyInt("track-list/count")!!

    for (i in 0 until count) {
      val type = MPVLib.getPropertyString("track-list/$i/type") ?: continue
      val isAudioTrack = type == "audio"
      val isSubTrack = type == "sub"
      val isVideoTrack = type == "video"
      if (!isAudioTrack && !isSubTrack && !isVideoTrack) {
        continue
      }
      val mpvId = MPVLib.getPropertyInt("track-list/$i/id") ?: continue
      val lang = MPVLib.getPropertyString("track-list/$i/lang")
      val title = MPVLib.getPropertyString("track-list/$i/title")

      Log.d(TAG, "TRACK $type : $mpvId $lang $title !")
    }

    val videoWidth = MPVLib.getPropertyInt("width")
    val videoHeight = MPVLib.getPropertyInt("height")
    Log.d(TAG, "W: $videoWidth $videoHeight !")
  }

  // Player commands

  fun seek(time: Double) {
    MPVLib.setPropertyDouble("time-pos", time)
  }

  fun setRepeatModifier(repeatparam: Boolean) {
    if (repeatparam != repeat) {
      repeat = repeatparam
      MPVLib.setPropertyString("loop-playlist", if (repeatparam) "inf" else "no")
      MPVLib.setPropertyString("loop-file", if (repeatparam) "inf" else "no")
    }
  }

  fun setTextTrackDelay(newDelay: Double) {
    if (newDelay != spuDelay) {
      spuDelay = newDelay
      MPVLib.setPropertyDouble("sub-delay", newDelay)
    }
  }

  fun setMutedModifier(mutedparam: Boolean) {
    if (muted != mutedparam) {
      muted = mutedparam
      MPVLib.setPropertyBoolean("mute", mutedparam)
    }
  }

  fun setVolumeModifier(vol: Int) {
    if (vol == volume) return
    volume = vol
    MPVLib.setPropertyInt("volume", volume)
  }

  fun setTextIdTrack(trackId: Int) {
    if (trackId != selectedTextTrack) {
      selectedTextTrack = trackId
      if (trackId == -1) {
        // Disable text track, default value
        MPVLib.setPropertyString("sid", "no")
      } else {
        MPVLib.setPropertyInt("sid", trackId)
      }
    }
  }

  fun setAudioIdTrack(trackId: Int) {
    if (trackId != selectedAudioTrack) {
      selectedAudioTrack = trackId
    }
  }
  fun setResizeMode(mode: String?) {}

  fun setSource(source: VideoSrc) {
    Log.d(TAG, "reset set")

    if (source.isEquals(src)) {
      return
    }

    if (source.uri != null) {
      src = source

      if (source.headers.size > 0) {
        // Combine all headers into a single comma-separated string
        val httpHeaderString =
                source.headers
                        .map { it.key + ": " + it.value.replace(",", "\\,") }
                        .joinToString(",")
        // Set all headers at once using the correct MPV format
        MPVLib.setOptionString("http-header-fields", httpHeaderString)
      }

      MPVLib.setOptionString(
              "start",
              if (source.startPosition >= 0) source.startPosition.toString() else ""
      )

      MPVLib.command(arrayOf("loadfile", source.uri.toString()))

      val sideloadTracks = source.sideLoadedTextTracks

      if (sideloadTracks != null) {
        for (externText in sideloadTracks.tracks) {
          MPVLib.command(
                  arrayOf(
                          "sub-add",
                          externText.uri,
                          "auto",
                          externText.title ?: "",
                          externText.language ?: ""
                  )
          )
        }
      }

      eventEmitter.onVideoLoadStart()
    } else {
      unloadMedia()
    }
  }

  fun setPausedModifier(pause: Boolean) {
    if (pause != paused) {
      paused = pause
      MPVLib.setPropertyBoolean("pause", paused)
    }
  }

  private fun cleanVariables() {
    src = VideoSrc()
  }

  private fun unloadMedia() {
    cleanVariables()
    MPVLib.command(arrayOf("stop"))
  }

  /** Sets the VO to use. It is automatically disabled/enabled when the surface dis-/appears. */
  fun setVo(vo: String) {
    if (vo != voInUse) {
      voInUse = vo
      MPVLib.setOptionString("vo", vo)
    }
  }

  // React host callbacks
  override fun onHostDestroy() {
    destroy()
  }

  override fun onHostPause() {
    activityIsForeground = false
    if (!playerDestoryed) {
      setPausedModifier(true)
    }
  }

  override fun onHostResume() {
    if (activityIsForeground) {
      return
    }
    activityIsForeground = true
  }

  // Surface callbacks

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    MPVLib.setPropertyString("android-surface-size", "${width}x$height")
  }

  override fun surfaceCreated(holder: SurfaceHolder) {
    Log.w(TAG, "attaching surface")
    MPVLib.attachSurface(holder.surface)
    // This forces mpv to render subs/osd/whatever into our surface even if it would ordinarily not
    MPVLib.setOptionString("force-window", "yes")

    if (filePath != null) {
      MPVLib.command(arrayOf("loadfile", filePath as String))
      filePath = null
    } else {
      // We disable video output when the context disappears, enable it back
      MPVLib.setPropertyString("vo", voInUse)
    }
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    Log.w(TAG, "detaching surface")
    MPVLib.setPropertyString("vo", "null")
    MPVLib.setOptionString("force-window", "no")
    MPVLib.detachSurface()
  }

  companion object {
    internal const val TAG = "VideoMpvView"
  }
}
