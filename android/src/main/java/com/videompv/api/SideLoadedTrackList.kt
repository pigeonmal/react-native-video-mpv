package com.videompv.api

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap

/**
 * Class representing a list of sideLoaded text track from application Do you use player import in
 * this class
 */
class SideLoadedTrackList {
    var tracks = ArrayList<SideLoadedTrack>()

    /** return true if this and src are equals */
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is SideLoadedTrackList) return false
        return tracks == other.tracks
    }

    companion object {
        fun parse(src: ReadableArray?): SideLoadedTrackList? {
            if (src == null) {
                return null
            }
            val sideLoadedTextTrackList = SideLoadedTrackList()
            for (i in 0 until src.size()) {
                val textTrack: ReadableMap? = src.getMap(i)
                if (textTrack != null) {
                    val parsedTrack = SideLoadedTrack.parse(textTrack)
                    if (parsedTrack != null) {
                        sideLoadedTextTrackList.tracks.add(parsedTrack)
                    }
                }
            }
            return sideLoadedTextTrackList
        }
    }
}