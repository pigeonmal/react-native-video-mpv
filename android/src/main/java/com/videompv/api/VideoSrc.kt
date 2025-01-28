package com.videompv.api

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.text.TextUtils
import com.facebook.react.bridge.ReadableMap
import com.videompv.toolbox.ReactBridgeUtils.safeGetArray
import com.videompv.toolbox.ReactBridgeUtils.safeGetFloat
import com.videompv.toolbox.ReactBridgeUtils.safeGetInt
import com.videompv.toolbox.ReactBridgeUtils.safeGetString
import java.util.Locale
import java.util.Objects

/**
 * Class representing VideoSrc props for host. Only generic code here, no reference to the player.
 */
class VideoSrc {
    /** String value of source to playback */
    var uriString: String? = null

    /** Parsed value of source to playback */
    var uri: Uri? = null

    /** Start position of playback used to resume playback */
    var startPosition: Float = -1f

    /** Allowed reload before failure notification */
    var minLoadRetryCount = 3

    /** HTTP header list */
    val headers: MutableMap<String, String> = HashMap()

    /** Media options for VLC */
    var mediaOptions = ArrayList<String>()

    /** The list of sideLoaded text tracks */
    var sideLoadedTextTracks: SideLoadedTrackList? = null

    override fun hashCode(): Int =
            Objects.hash(uriString, uri, startPosition, headers, mediaOptions)

    /** Return true if this and src are equal */
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is VideoSrc) return false
        return (uri == other.uri &&
                startPosition == other.startPosition &&
                minLoadRetryCount == other.minLoadRetryCount &&
                mediaOptions == other.mediaOptions &&
                sideLoadedTextTracks == other.sideLoadedTextTracks)
    }

    /** return true if this and src are equals */
    fun isEquals(source: VideoSrc): Boolean = this == source

    companion object {
        private const val TAG = "VideoSrc"
        private const val PROP_SRC_URI = "uri"
        private const val PROP_SRC_START_POSITION = "startPosition"
        private const val PROP_SRC_HEADERS = "requestHeaders"
        private const val PROP_SRC_MEDIA_OPTIONS = "mediaOptions"
        private const val PROP_SRC_TEXT_TRACKS = "textTracks"
        private const val PROP_SRC_MIN_LOAD_RETRY_COUNT = "minLoadRetryCount"

        @SuppressLint("DiscouragedApi")
        private fun getUriFromAssetId(context: Context, uriString: String): Uri? {
            val resources: Resources = context.resources
            val packageName: String = context.packageName
            var identifier = resources.getIdentifier(uriString, "drawable", packageName)
            if (identifier == 0) {
                identifier = resources.getIdentifier(uriString, "raw", packageName)
            }

            if (identifier <= 0) {
                // cannot find identifier of content
                return null
            }
            return Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .path(identifier.toString())
                    .build()
        }

        /** Parse the source ReadableMap received from app */
        @JvmStatic
        fun parse(src: ReadableMap?, context: Context): VideoSrc {
            val videoSrc = VideoSrc()

            if (src != null) {
                val uriString = safeGetString(src, PROP_SRC_URI, null)
                if (uriString == null || TextUtils.isEmpty(uriString)) {
                    return videoSrc
                }
                var uri = Uri.parse(uriString)
                if (uri == null) {
                    // return an empty source
                    return videoSrc
                } else if (!isValidScheme(uri.scheme)) {
                    uri = getUriFromAssetId(context, uriString)
                    if (uri == null) {
                        // cannot find identifier of content
                        return videoSrc
                    }
                }
                videoSrc.uriString = uriString
                videoSrc.uri = uri
                videoSrc.startPosition = safeGetFloat(src, PROP_SRC_START_POSITION, -1f)
                videoSrc.minLoadRetryCount = safeGetInt(src, PROP_SRC_MIN_LOAD_RETRY_COUNT, 3)

                val propSrcHeadersArray = safeGetArray(src, PROP_SRC_HEADERS)
                if (propSrcHeadersArray != null && propSrcHeadersArray.size() > 0) {
                    for (i in 0 until propSrcHeadersArray.size()) {
                        val current: ReadableMap? = propSrcHeadersArray.getMap(i)
                        val key = current?.getString("key")
                        val value = current?.getString("value")
                        if (key != null && value != null) {
                            videoSrc.headers[key] = value
                        }
                    }
                }

                val mediaOptionsArray = safeGetArray(src, PROP_SRC_MEDIA_OPTIONS)
                if (mediaOptionsArray != null && mediaOptionsArray.size() > 0) {
                    for (i in 0 until mediaOptionsArray.size()) {
                        val mediaOpt: String? = mediaOptionsArray.getString(i)
                        if (mediaOpt != null) {
                            videoSrc.mediaOptions.add(mediaOpt)
                        }
                    }
                }

                videoSrc.sideLoadedTextTracks =
                        SideLoadedTrackList.parse(safeGetArray(src, PROP_SRC_TEXT_TRACKS))
            }
            return videoSrc
        }

        /** Return true if URI scheme is supported for Android playback */
        private fun isValidScheme(scheme: String?): Boolean {
            if (scheme == null) {
                return false
            }
            val lowerCaseUri = scheme.lowercase(Locale.getDefault())
            return (lowerCaseUri == "http" ||
                    lowerCaseUri == "https" ||
                    lowerCaseUri == "content" ||
                    lowerCaseUri == "file" ||
                    lowerCaseUri == "rtsp" ||
                    lowerCaseUri == "asset")
        }
    }
}