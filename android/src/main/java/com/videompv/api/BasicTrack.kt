package com.videompv.api

open class BasicTrack(
        var title: String? = null,
        var language: String? = null,
        val id: Int,
        val selected: Boolean = false
)
