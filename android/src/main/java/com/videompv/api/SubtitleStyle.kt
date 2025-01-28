package com.videompv.api

import com.facebook.react.bridge.ReadableMap
import com.videompv.toolbox.ReactBridgeUtils.safeGetBool
import com.videompv.toolbox.ReactBridgeUtils.safeGetInt

class SubtitleStyle(val options: ArrayList<String>) {

    companion object {
        private const val PROP_SUB_FONTSIZE = "fontSize"
        

        private const val PROP_SUB_COLOR = "color"

        private const val PROP_SUB_BOLD = "bold"

        private const val PROP_SUB_BACK_OPACITY = "backgroundOpacity"

        private const val PROP_SUB_BACK_COLOR = "backgroundColor"

        @JvmStatic
        fun parse(style: ReadableMap?): SubtitleStyle {
            val options = ArrayList<String>()
          /*
            var fontSize = safeGetInt(style, PROP_SUB_FONTSIZE, 0)
            if (fontSize !in VALID_SUB_VALUES) {
                fontSize = 0
            }
            options.add(OPTION_FONTSIZE + fontSize)

            val fontColor = safeGetInt(style, PROP_SUB_COLOR, 0x00ffffff)
            options.add(OPTION_COLOR + fontColor)

            val fontBold = safeGetBool(style, PROP_SUB_BOLD, false)
            options.add(if (fontBold) OPTION_BOLD else OPTION_NOT_BOLD)

            var backOpacity = safeGetInt(style, PROP_SUB_BACK_OPACITY, 0)
            if (backOpacity !in 0..255) {
                backOpacity = 0
            }
            options.add(OPTION_BACK_OPACITY + backOpacity)

            val backColor = safeGetInt(style, PROP_SUB_BACK_COLOR, 0)
            options.add(OPTION_BACK_COLOR + backColor)
            */

            return SubtitleStyle(options)
        }
    }
}