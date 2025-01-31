package com.videompv.api

class VideoBasicTrack(
        title: String? = null,
        language: String? = null,
        id: Int,
        selected: Boolean = false,
        val width: Int = 0,
        val height: Int = 0,
) : BasicTrack(title, language, id, selected)
