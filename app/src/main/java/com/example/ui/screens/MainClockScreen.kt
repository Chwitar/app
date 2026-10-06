package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AvailableThemes
import com.example.model.ClockDisplayMode
import com.example.model.ClockThemeOption
import com.example.ui.components.AnalogClock
import com.example.ui.components.DigitalClock
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark
import com.example.viewmodel.ClockUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainClockScreen(
    uiState: ClockUiState,
    onSetDisplayMode: (ClockDisplayMode) -> Unit,
    onToggle24Hour: () -> Unit,
    onToggleSeconds: () -> Unit,
    onSetTheme: (ClockThemeOption) -> Unit,
    onOpenNightstand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val calendar = uiState.currentCalendar
    val accentColor = uiState.selectedTheme.color

    // Date formatting in Arabic
    val arabicLocale = remember { Locale("ar") }
    val dayOfWeekFormat = remember { SimpleDateFormat("EEEE", arabicLocale) }
    val fullDateFormat = remember { SimpleDateFormat("d MMMM yyyy", arabicLocale) }

    val dayName = remember(calendar.timeInMillis) { dayOfWeekFormat.format(calendar.time) }
    val fullDate = remember(calendar.timeInMillis) { fullDateFormat.format(calendar.time) }

    val tz = remember { TimeZone.getDefault() }
    val gmtOffsetHours = tz.rawOffset / (1000 * 60 * 60)
    val gmtString = if (gmtOffsetHours >= 0) "GMT+$gmtOffsetHours" else "GMT$gmtOffsetHours"

    Column(
        modifier = modifier
            .testTag("main_clock_screen")
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Switcher Bar (Digital vs Analog) & Nightstand Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode Segmented Capsule
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Slate900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    ClockDisplayMode.entries.forEach { mode ->
                        val isSelected = uiState.displayMode == mode
                        Box(
                            modifier = Modifier
                                .testTag("mode_toggle_${mode.name.lowercase()}")
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) accentColor else Color.Transparent)
                                .clickable { onSetDisplayMode(mode) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.labelArabic,
                                color = if (isSelected) Color.Black else TextSecondaryDark,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Nightstand Mode Launcher
            OutlinedButton(
                onClick = onOpenNightstand,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("open_nightstand_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = "وضع الشاشة الليلية",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الوضع الليلي",
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clock Display (Digital or Analog)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = uiState.displayMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "clock_mode_content"
            ) { mode ->
                when (mode) {
                    ClockDisplayMode.DIGITAL -> {
                        DigitalClock(
                            calendar = calendar,
                            is24Hour = uiState.is24HourFormat,
                            showSeconds = uiState.showSeconds,
                            accentColor = accentColor
                        )
                    }
                    ClockDisplayMode.ANALOG -> {
                        AnalogClock(
                            calendar = calendar,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Date & Day Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate900.copy(alpha = 0.7f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = dayName,
                            color = TextPrimaryDark,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = fullDate,
                            color = TextSecondaryDark,
                            fontSize = 13.sp
                        )
                    }
                }

                // Timezone Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate800)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = TextTertiaryDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = gmtString,
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Toggles & Preferences Section
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.6f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "خيارات العرض",
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.is24HourFormat,
                        onClick = onToggle24Hour,
                        label = { Text(if (uiState.is24HourFormat) "نظام 24 ساعة (مفعل)" else "نظام 12 ساعة (ص/م)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor,
                            labelColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("toggle_24h_chip")
                    )

                    FilterChip(
                        selected = uiState.showSeconds,
                        onClick = onToggleSeconds,
                        label = { Text(if (uiState.showSeconds) "إظهار الثواني ✓" else "إخفاء الثواني") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor,
                            labelColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("toggle_seconds_chip")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme color selection
                Text(
                    text = "لون الطابع",
                    color = TextSecondaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvailableThemes.forEach { theme ->
                        val isSelected = uiState.selectedTheme.id == theme.id
                        Box(
                            modifier = Modifier
                                .testTag("theme_color_${theme.id}")
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.color)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(2.dp, Color.White, CircleShape)
                                    } else Modifier
                                )
                                .clickable { onSetTheme(theme) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Informative stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)

            StatMiniCard(
                title = "اليوم في السنة",
                value = "$dayOfYear / 365",
                modifier = Modifier.weight(1f)
            )
            StatMiniCard(
                title = "أسبوع السنة",
                value = "الأسبوع $weekOfYear",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatMiniCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Slate900.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                color = TextTertiaryDark,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = TextPrimaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
