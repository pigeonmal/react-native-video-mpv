package com.videompv.api

import com.facebook.react.bridge.ReadableMap
import com.videompv.toolbox.ReactBridgeUtils.safeGetBool
import com.videompv.toolbox.ReactBridgeUtils.safeGetInt
import com.videompv.toolbox.ReactBridgeUtils.safeGetString

class SubtitleStyle(
        val fontSize: Int = 55,
        val color: String = "1.0/1.0/1.0",
        val bold: Boolean = false,
        val backgroundColor: String = "0.0/0.0/0.0/0.0",
        val borderStyle: String = "outline-and-shadow"
) {

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is SubtitleStyle) return false
        return fontSize == other.fontSize &&
                color == other.color &&
                bold == other.bold &&
                backgroundColor == other.backgroundColor &&
                borderStyle == other.borderStyle
    }

    companion object {
        private const val PROP_SUB_FONTSIZE = "fontSize"
        private const val PROP_SUB_COLOR = "color"
        private const val PROP_SUB_BOLD = "bold"
        private const val PROP_SUB_BACK_COLOR = "backgroundColor"
        private const val PROP_SUB_BORDER_STYLE = "borderStyle"

        fun parse(style: ReadableMap?): SubtitleStyle {
            var fontSize = safeGetInt(style, PROP_SUB_FONTSIZE, 55)
            val fontColor = safeGetString(style, PROP_SUB_COLOR) ?: "1.0/1.0/1.0"
            val fontBold = safeGetBool(style, PROP_SUB_BOLD, false)
            val backColor = safeGetString(style, PROP_SUB_BACK_COLOR) ?: "0.0/0.0/0.0/0.0"
            val borderStyle = safeGetString(style, PROP_SUB_BORDER_STYLE) ?: "outline-and-shadow"

            return SubtitleStyle(fontSize, fontColor, fontBold, backColor, borderStyle)
        }
    }
}
