package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PurpleAccent

enum class ClockDisplayMode(val labelArabic: String) {
    DIGITAL("رقمية"),
    ANALOG("عقارب")
}

data class ClockThemeOption(
    val id: String,
    val nameArabic: String,
    val color: Color
)

val AvailableThemes = listOf(
    ClockThemeOption("cyan", "أزرق سماوي", CyanAccent),
    ClockThemeOption("amber", "كهرمان دافئ", AmberAccent),
    ClockThemeOption("emerald", "أخضر زمردي", EmeraldAccent),
    ClockThemeOption("purple", "بنفسجي مشع", PurpleAccent),
    ClockThemeOption("white", "أبيض نقي", Color(0xFFF1F5F9))
)

data class WorldCity(
    val id: String,
    val nameArabic: String,
    val countryArabic: String,
    val timeZoneId: String,
    val flag: String,
    val isFavorite: Boolean = false
)

data class LapItem(
    val lapNumber: Int,
    val lapDurationMs: Long,
    val totalDurationMs: Long
)

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED
}
