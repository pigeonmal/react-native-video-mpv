package com.videompv.api

open class BasicTrack(
        var title: String? = null,
        var language: String? = null,
        val id: Int,
        val selected: Boolean = false
) {
        companion object {
                /*
                private const val UNDEFINED_LANGUAGE_CODE = "und"
                private const val LANGUAGE_CODE_LENGTH = 2
  fun parse(
                        vlcTrack: Track,
                        currentSelectedId: Int,
                        sideTrack: SideLoadedTrack?
                ): BasicTrack {
                        val language = vlcTrack.language?.lowercase()
                        val languageProcessed =
                                if (language == UNDEFINED_LANGUAGE_CODE) null
                                else language?.take(LANGUAGE_CODE_LENGTH)

                        return BasicTrack(
                                vlcTrack.description ?: sideTrack?.title,
                                languageProcessed ?: sideTrack?.language,
                                vlcTrack.id,
                                currentSelectedId == vlcTrack.id
                        )
                }
                */


        }
}