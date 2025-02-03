package com.videompv.api

open class BasicTrack(
        var title: String?,
        var language: String?,
        val id: Int,
        val selected: Boolean,
        val external: Boolean
)
