package com.videompv.api

class VideoBasicTrack(
        title: String?,
        language: String?,
        id: Int,
        selected: Boolean,
        external: Boolean,
        val width: Int,
        val height: Int,
) : BasicTrack(title, language, id, selected, external)
