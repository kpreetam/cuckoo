package com.example.cuckooclock

import java.util.Calendar
import kotlin.math.*

object MoonPhase {
    data class MoonInfo(val fraction: Double, val phaseName: String, val trending: String, val emoji: String)

    fun calculate(): MoonInfo {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        var y = year; var m = month
        if (m <= 2) { y--; m += 12 }
        val a = y / 100
        val b = 2 - a + a / 4
        val jd = (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + b - 1524.5
        val phase = ((( jd - 2451549.5) % 29.53058867) + 29.53058867) % 29.53058867
        val fraction = phase / 29.53058867

        val phaseName = when {
            phase < 1.85 -> "New Moon"; phase < 7.38 -> "Waxing Crescent"
            phase < 9.22 -> "First Quarter"; phase < 14.77 -> "Waxing Gibbous"
            phase < 16.61 -> "Full Moon"; phase < 22.15 -> "Waning Gibbous"
            phase < 23.99 -> "Last Quarter"; else -> "Waning Crescent"
        }
        val trending = if (fraction < 0.5) "Trending → Full Moon" else "Trending → New Moon"
        val emoji = when {
            fraction < 0.0625 -> "🌑"; fraction < 0.1875 -> "🌒"
            fraction < 0.3125 -> "🌓"; fraction < 0.4375 -> "🌔"
            fraction < 0.5625 -> "🌕"; fraction < 0.6875 -> "🌖"
            fraction < 0.8125 -> "🌗"; fraction < 0.9375 -> "🌘"; else -> "🌑"
        }
        return MoonInfo(fraction, phaseName, trending, emoji)
    }
}
