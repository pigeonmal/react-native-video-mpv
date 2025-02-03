package com.videompv

import android.content.res.AssetManager
import android.os.Build
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext
import com.videompv.MPVLib.mpvEventId
import com.videompv.MPVLib.mpvFormat
import com.videompv.api.BasicTrack
import com.videompv.api.LangsPref
import com.videompv.api.SubtitleStyle
import com.videompv.api.VideoBasicTrack
import com.videompv.api.VideoSrc
import java.io.File
import java.io.FileOutputStream

class VideoMpvView(context: ThemedReactContext) :
        SurfaceView(context), SurfaceHolder.Callback, LifecycleEventListener, MPVLib.EventObserver {

  /* SYSTEM */
  internal final val eventEmitter = VideoMpvEventEmitter()
  private val mThemedReactContext: ThemedReactContext = context
  private var activityIsForeground = true
  private var playerDestoryed = false
  private var isInHostPause = false
  private var playerParsed = false
  private var filePath: String? = null
  private var hwdec: String = "auto" // https://mpv.io/manual/stable/#options-hwdec
  private var voInUse: String = "gpu" // https://mpv.io/manual/stable/#video-output-drivers-vo

  /* PROPS */
  private var src: VideoSrc = VideoSrc()
  private var paused: Boolean = false
  private var muted: Boolean = false
  private var volume = 100
  private var repeat: Boolean = false // Default true by mpv but false in my library
  private var langsPref: LangsPref = LangsPref("", "", true)
  private var subStyle: SubtitleStyle = SubtitleStyle()
  // private var scaleType: String = ScaleType.SURFACE_BEST_FIT
  private var spuDelay: Double = 0.0

  init {
    mThemedReactContext.addLifecycleEventListener(this)
    val appContext = context.applicationContext
    val mpvDir = File(appContext.getExternalFilesDir(null) ?: appContext.filesDir, "mpv")

    Log.d(TAG, "mpv config dir: $mpvDir")

    if (!mpvDir.exists()) mpvDir.mkdirs()

    arrayOf("subfont.ttf").forEach { fileName ->
      val file = File(mpvDir, fileName)
      if (file.exists()) return@forEach
      appContext.assets.open(fileName, AssetManager.ACCESS_STREAMING).copyTo(FileOutputStream(file))
    }

    MPVLib.create(context)
    MPVLib.setOptionString("config", "yes")
    MPVLib.setOptionString("config-dir", mpvDir.path)
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

    setLangsPref(LangsPref.parse(null)) // language default os

    MPVLib.setOptionString("tls-verify", "no")
    MPVLib.setOptionString("sub-font-provider", "none")
    MPVLib.setOptionString("keep-open", "always")
    MPVLib.setOptionString("sub-scale-with-window", "yes")

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
    MPVLib.setPropertyString("loop-playlist", "no")
    MPVLib.setOptionString("save-position-on-quit", "no")
  }

  /**
   * Deinitialize libmpv.
   *
   * Call this once before the view is destroyed.
   */
  fun destroy() {
    cleanVariables()
    playerDestoryed = true
    mThemedReactContext.removeLifecycleEventListener(this)
    MPVLib.removeObserver(this)

    // Disable surface callbacks to avoid using unintialized mpv state
    holder.removeCallback(this)

    MPVLib.destroy()
  }

  fun observeProperties() {
    // MPVLib.observeProperty("eof-reached", MPVFormat.MPV_FORMAT_NONE)
    //  MPVLib.observeProperty("core-idle", MPVFormat.MPV_FORMAT_FLAG) // Like "pause"
    //  MPVLib.observeProperty("pause", MPVFormat.MPV_FORMAT_FLAG)
    MPVLib.observeProperty("time-pos", mpvFormat.MPV_FORMAT_INT64)
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
      mpvEventId.MPV_EVENT_FILE_LOADED -> onVideoLoaded()
      mpvEventId.MPV_EVENT_START_FILE -> onVideoLoadStart()
    }
  }

  override fun eventFileEnd(reason: Int, error: String?) {
    Log.d(TAG, "Video End $reason")
    cleanVariables()

    eventEmitter.onVideoStop(reason)
  }

  private fun updatePlaybackPos(pos: Long) {
    if (playerDestoryed || !playerParsed) return
    Log.d(TAG, "PROGRESS $pos")
    eventEmitter.onVideoProgress(
            pos.toDouble(),
            (MPVLib.getPropertyDouble("duration") ?: 0.0),
            (MPVLib.getPropertyDouble("percent-pos") ?: 0.0) / 100.0
    )
  }

  private fun onVideoLoadStart() {
    eventEmitter.onVideoLoadStart()
  }

  private fun onVideoLoaded() {
    Log.d(TAG, "Video loaded !")
    playerParsed = true

    val sideloadTracks = src.sideLoadedTextTracks

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

    val textTracks = ArrayList<BasicTrack>()
    val audioTracks = ArrayList<BasicTrack>()
    val videosTracks = ArrayList<VideoBasicTrack>()

    val count = MPVLib.getPropertyInt("track-list/count")!!
    Log.d(TAG, "COUNT $count")
    for (i in 0 until count) {
      val type = MPVLib.getPropertyString("track-list/$i/type") ?: continue
      val isAudioTrack = type == "audio"
      val isSubTrack = type == "sub"
      val isVideoTrack = type == "video"
      if (!isAudioTrack && !isSubTrack && !isVideoTrack) {
        continue
      }
      val mpvId = MPVLib.getPropertyInt("track-list/$i/id") ?: continue
      val lang: String? = MPVLib.getPropertyString("track-list/$i/lang")
      val title: String? = MPVLib.getPropertyString("track-list/$i/title")
      val selected = MPVLib.getPropertyString("track-list/$i/selected") == "yes"

      Log.d(TAG, "$type $mpvId $lang $title $selected")
      if (isAudioTrack) {
        audioTracks.add(BasicTrack(title, lang, mpvId, selected))
      } else if (isSubTrack) {
        textTracks.add(BasicTrack(title, lang, mpvId, selected))
      } else if (isVideoTrack) {
        videosTracks.add(
                VideoBasicTrack(
                        title,
                        lang,
                        mpvId,
                        selected,
                        MPVLib.getPropertyInt("track-list/$i/demux-w"), // Width
                        MPVLib.getPropertyInt("track-list/$i/demux-h") // Height
                )
        )
      }
    }

    eventEmitter.onVideoLoad(
            MPVLib.getPropertyDouble("duration") ?: 0.0,
            MPVLib.getPropertyDouble("time-pos") ?: 0.0,
            MPVLib.getPropertyInt("width") ?: 0,
            MPVLib.getPropertyInt("height") ?: 0,
            audioTracks,
            textTracks,
            videosTracks
    )
  }

  // Player commands

  fun seek(time: Double) {
    if (playerDestoryed || !playerParsed) return
    MPVLib.setPropertyDouble("time-pos", time)
  }

  fun setRepeatModifier(repeatparam: Boolean) {
    if (repeatparam != repeat) {
      repeat = repeatparam
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
    MPVLib.setPropertyInt("volume", vol)
  }

  fun setLangsPref(newPrefs: LangsPref) {
    if (langsPref != newPrefs) {
      langsPref = newPrefs
      MPVLib.setPropertyString("alang", newPrefs.alang)
      MPVLib.setPropertyString("slang", newPrefs.slang)
      MPVLib.setPropertyString(
              "subs-with-matching-audio",
              if (newPrefs.subMatchingAudio) "yes" else "no"
      )
    }
  }

  fun setSubStyle(newStyle: SubtitleStyle) {
    if (subStyle != newStyle) {
      subStyle = newStyle
      MPVLib.setPropertyInt("sub-font-size", newStyle.fontSize)
      MPVLib.setPropertyString("sub-color", newStyle.color)
      MPVLib.setPropertyString("sub-bold", if (newStyle.bold) "yes" else "no")
      MPVLib.setPropertyString("sub-back-color", newStyle.backgroundColor)
    }
  }

  fun setPlayerPropertyString(key: String, value: String) {
    if (playerDestoryed) return
    MPVLib.setPropertyString(key, value)
  }

  fun setPlayerPropertyInt(key: String, value: Int) {
    if (playerDestoryed) return
    MPVLib.setPropertyInt(key, value)
  }

  fun setResizeMode(mode: String?) {}

  fun setSource(source: VideoSrc) {
    Log.d(TAG, "reset set")

    if (source.isEquals(src)) {
      return
    }

    unloadMedia()
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

      MPVLib.setOptionString("sid", "auto")
      MPVLib.setOptionString("aid", "auto")

      MPVLib.setOptionString(
              "start",
              if (source.startPosition >= 0) source.startPosition.toString() else ""
      )

      if (MPVLib.getPropertyString("force-window") == "yes") {
        MPVLib.command(arrayOf("loadfile", source.uri.toString()))
      } else {
        filePath = source.uri.toString()
      }
    } else {
      MPVLib.command(arrayOf("stop"))
    }
  }

  fun setPausedModifier(pause: Boolean) {
    if (pause != paused) {
      paused = pause
      MPVLib.setPropertyBoolean("pause", paused)
    }
  }

  private fun cleanVariables() {
    filePath = null
    playerParsed = false
    src = VideoSrc()
  }

  private fun unloadMedia() {
    cleanVariables()
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
    if (!playerDestoryed && !paused) {
      paused = true
      isInHostPause = true
      MPVLib.setPropertyBoolean("pause", true)
    }
  }

  override fun onHostResume() {
    activityIsForeground = true
    if (isInHostPause) {
      isInHostPause = false
      paused = false
      MPVLib.setPropertyBoolean("pause", false)
    }
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
