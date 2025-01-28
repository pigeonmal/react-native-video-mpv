package com.videompv.api


class VideoBasicTrack(
        title: String? = null,
        language: String? = null,
        id: Int,
        selected: Boolean = false,
        val width: Int = 0,
        val height: Int = 0,
) : BasicTrack(title, language, id, selected) {
    companion object {
      /*
        fun parse(vlcVideoTrack: VideoTrack, currentSelectedIdVideo: Int): VideoBasicTrack =
                BasicTrack.parse(vlcVideoTrack, currentSelectedIdVideo, null).let {
                    VideoBasicTrack(
                            it.title,
                            it.language,
                            it.id,
                            it.selected,
                            vlcVideoTrack.width,
                            vlcVideoTrack.height
                    )
                }
      */
    }
}