package com.videompv

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.EventDispatcher
import com.videompv.api.BasicTrack
import com.videompv.api.VideoBasicTrack

enum class EventTypes(val eventName: String) {
    EVENT_LOAD_START("onVideoLoadStart"),
    EVENT_LOAD("onVideoLoad"),
    EVENT_ERROR("onVideoError"),
    EVENT_PROGRESS("onVideoProgress"),
    EVENT_STOP("onVideoStop"),
    EVENT_EOF_REACHED("onVideoEndReached"),
    EVENT_BUFFER("onVideoBuffer"),
    EVENT_PLAYBACK_STATE_CHANGED("onVideoPlaybackStateChanged");

    companion object {
        fun toMap() =
                mutableMapOf<String, Any>().apply {
                    EventTypes.values().toList().forEach { eventType ->
                        put(
                                "top${eventType.eventName.removePrefix("on")}",
                                hashMapOf("registrationName" to eventType.eventName)
                        )
                    }
                }
    }
}

class VideoMpvEventEmitter {
    lateinit var onVideoLoadStart: () -> Unit
    lateinit var onVideoLoad:
            (
                    duration: Double,
                    currentPosition: Double,
                    videoWidth: Int,
                    videoHeight: Int,
                    audioTracks: List<BasicTrack>,
                    textTracks: List<BasicTrack>,
                    videoTracks: List<VideoBasicTrack>,
            ) -> Unit
    lateinit var onVideoError: (errorString: String, errorCode: Int) -> Unit
    lateinit var onVideoProgress:
            (currentPosition: Double, seekableDuration: Double, progress: Double) -> Unit
    lateinit var onVideoPlaybackStateChanged: (isPlaying: Boolean, isSeeking: Boolean) -> Unit
    lateinit var onVideoEndReached: () -> Unit
    lateinit var onVideoStop: (reason: Int) -> Unit
    lateinit var onVideoBuffer: (isBuffering: Boolean) -> Unit

    fun addEventEmitters(reactContext: ThemedReactContext, view: VideoMpvView) {
        val dispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext, view.id)
        val surfaceId = UIManagerHelper.getSurfaceId(reactContext)
        if (dispatcher != null) {
            val event = EventBuilder(surfaceId, view.id, dispatcher)

            onVideoLoadStart = { event.dispatch(EventTypes.EVENT_LOAD_START) }
            onVideoLoad =
                    {
                            duration,
                            currentPosition,
                            videoWidth,
                            videoHeight,
                            audioTracks,
                            textTracks,
                            videoTracks ->
                        event.dispatch(EventTypes.EVENT_LOAD) {
                            putDouble("duration", duration)
                            putDouble("currentTime", currentPosition)

                            val naturalSize: WritableMap =
                                    aspectRatioToNaturalSize(videoWidth, videoHeight)
                            putMap("naturalSize", naturalSize)
                            putArray("videoTracks", videotracksToArray(videoTracks))
                            putArray("audioTracks", basictracksToArray(audioTracks))
                            putArray("textTracks", basictracksToArray(textTracks))
                        }
                    }

            onVideoError = { errorString, errorCode ->
                event.dispatch(EventTypes.EVENT_ERROR) {
                    putMap(
                            "error",
                            Arguments.createMap().apply {
                                putString("errorString", errorString)
                                putInt("errorCode", errorCode)
                            }
                    )
                }
            }
            onVideoProgress = { currentPosition, seekableDuration, progress ->
                event.dispatch(EventTypes.EVENT_PROGRESS) {
                    putDouble("currentTime", currentPosition)
                    putDouble("seekableDuration", seekableDuration)
                    putDouble("progress", progress)
                }
            }
            onVideoPlaybackStateChanged = { isPlaying, isSeeking ->
                event.dispatch(EventTypes.EVENT_PLAYBACK_STATE_CHANGED) {
                    putBoolean("isPlaying", isPlaying)
                    putBoolean("isSeeking", isSeeking)
                }
            }
            onVideoEndReached = { event.dispatch(EventTypes.EVENT_EOF_REACHED) }
            onVideoStop = { reason ->
                event.dispatch(EventTypes.EVENT_STOP) { putInt("reason", reason) }
            }

            onVideoBuffer = { isBuffering ->
                event.dispatch(EventTypes.EVENT_BUFFER) { putBoolean("isBuffering", isBuffering) }
            }
        }
    }


      private class VideoCustomEvent(surfaceId: Int, viewId: Int, private val event: EventTypes, private val paramsSetter: (WritableMap.() -> Unit)?) :
        Event<VideoCustomEvent>(surfaceId, viewId) {

        override fun getEventName(): String = "top${event.eventName.removePrefix("on")}"

        override fun getEventData(): WritableMap? = Arguments.createMap().apply(paramsSetter ?: {})
    }

    private class EventBuilder(private val surfaceId: Int, private val viewId: Int, private val dispatcher: EventDispatcher) {
        fun dispatch(event: EventTypes, paramsSetter: (WritableMap.() -> Unit)? = null) =
            dispatcher.dispatchEvent(VideoCustomEvent(surfaceId, viewId, event, paramsSetter))
    }

    private fun WritableMap.applyTrackProperties(format: BasicTrack) {
        putInt("id", format.id)
        putBoolean("selected", format.selected)
        format.title?.let { putString("title", it) }
        format.language?.let { putString("language", it) }
    }

    private fun <T : BasicTrack> Collection<T>?.toWritableArray(
            extraProperties: (WritableMap, T) -> Unit = { _, _ -> }
    ): WritableArray =
            Arguments.createArray().apply {
                this@toWritableArray?.forEach { format ->
                    pushMap(
                            Arguments.createMap().apply {
                                applyTrackProperties(format)
                                extraProperties(this, format)
                            }
                    )
                }
            }

    // Usage:
    private fun basictracksToArray(tracks: List<BasicTrack>?) = tracks.toWritableArray()

    private fun videotracksToArray(tracks: List<VideoBasicTrack>?) =
            tracks.toWritableArray { map, format ->
                map.putInt("width", format.width)
                map.putInt("height", format.height)
            }

    private fun aspectRatioToNaturalSize(videoWidth: Int, videoHeight: Int): WritableMap =
            Arguments.createMap().apply {
                if (videoWidth > 0) {
                    putInt("width", videoWidth)
                }
                if (videoHeight > 0) {
                    putInt("height", videoHeight)
                }

                val orientation =
                        when {
                            videoWidth > videoHeight -> "landscape"
                            videoWidth < videoHeight -> "portrait"
                            else -> "square"
                        }

                putString("orientation", orientation)
            }
}
