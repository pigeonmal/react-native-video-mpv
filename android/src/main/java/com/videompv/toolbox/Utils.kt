package com.videompv.toolbox

import kotlin.math.abs

object Utils {
    fun prettyTime(seconds: Float, sign: Boolean = false): String {
        if (sign) {
            return (if (seconds >= 0) "+" else "-") + prettyTime(abs(seconds))
        }

        val totalSeconds = seconds.toInt()
        val milliseconds = ((seconds - totalSeconds) * 1000).toInt()

        val hours = totalSeconds / 3600
        val minutes = totalSeconds % 3600 / 60
        val remainingSeconds = totalSeconds % 60

        return when {
            hours > 0 -> "%d:%02d:%02d.%03d".format(hours, minutes, remainingSeconds, milliseconds)
            minutes > 0 -> "%02d:%02d.%03d".format(minutes, remainingSeconds, milliseconds)
            else -> "%02d.%03d".format(remainingSeconds, milliseconds)
        }
    }
}
