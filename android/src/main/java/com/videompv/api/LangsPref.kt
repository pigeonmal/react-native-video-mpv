package com.videompv.api

import com.facebook.react.bridge.ReadableMap
import com.videompv.toolbox.ReactBridgeUtils.safeGetBool
import com.videompv.toolbox.ReactBridgeUtils.safeGetString
import java.util.Locale

class LangsPref(val alang: String, val slang: String, val subMatchingAudio: Boolean = true) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is LangsPref) return false
        return alang == other.alang &&
                slang == other.slang &&
                subMatchingAudio == other.subMatchingAudio
    }

    companion object {
        private const val PREFS_AUDIO = "audio"
        private const val PREFS_SUB = "sub"
        private const val PREFS_MATCHING_AUDIO = "subMatchingAudio"
        private val deviceLanguage = Locale.getDefault().isO3Language ?: ""

        fun parse(src: ReadableMap?): LangsPref {
            val alang = safeGetString(src, PREFS_AUDIO) ?: deviceLanguage
            val slang = safeGetString(src, PREFS_SUB) ?: deviceLanguage
            val subMatchingAudio = safeGetBool(src, PREFS_MATCHING_AUDIO, true)
            return LangsPref(alang, slang, subMatchingAudio)
        }
    }
}
