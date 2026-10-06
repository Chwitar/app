package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TimerStatus
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark
import com.example.viewmodel.TimerUiState
import java.util.Locale

private data class TimerPreset(val label: String, val seconds: Long)

private val timerPresets = listOf(
    TimerPreset("1 دقيقة", 60),
    TimerPreset("3 دقائق", 180),
    TimerPreset("5 دقائق", 300),
    TimerPreset("10 دقائق", 600),
    TimerPreset("15 دقيقة", 900),
    TimerPreset("25 دقيقة (تركيز)", 1500),
    TimerPreset("45 دقيقة", 2700)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerScreen(
    timerState: TimerUiState,
    accentColor: Color,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSetDuration: (Long) -> Unit,
    onAddMinute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val remaining = timerState.remainingSeconds
    val total = timerState.totalSeconds

    val hours = remaining / 3600
    val minutes = (remaining % 3600) / 60
    val seconds = remaining % 60

    val timeFormatted = if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    val progress = if (total > 0) (remaining.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .testTag("timer_screen")
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Timer Finished Alert Banner
        AnimatedVisibility(
            visible = timerState.status == TimerStatus.FINISHED,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EmeraldAccent.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = EmeraldAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "انتهى الوقت!",
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اكتمل العد التنازلي للمؤقت بنجاح",
                            color = EmeraldAccent,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Circular Countdown Progress
        Box(
            modifier = Modifier
                .size(260.dp)
                .testTag("timer_countdown_dial"),
            contentAlignment = Alignment.Center
        ) {
            // Background track
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = Slate800,
                strokeWidth = 10.dp
            )

            // Dynamic progress
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = if (timerState.status == TimerStatus.FINISHED) EmeraldAccent else accentColor,
                strokeWidth = 10.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    color = TextPrimaryDark,
                    fontSize = if (hours > 0) 36.sp else 46.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = when (timerState.status) {
                        TimerStatus.RUNNING -> "قيد التشغيل"
                        TimerStatus.PAUSED -> "متوقف مؤقتاً"
                        TimerStatus.FINISHED -> "مكتمل"
                        TimerStatus.IDLE -> "جاهز"
                    },
                    color = if (timerState.status == TimerStatus.FINISHED) EmeraldAccent else TextSecondaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions (Reset, Start/Pause, Add 1m)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            OutlinedButton(
                onClick = onReset,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("timer_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "إعادة ضبط",
                    tint = TextPrimaryDark
                )
            }

            // Start / Pause
            Button(
                onClick = {
                    if (timerState.status == TimerStatus.RUNNING) onPause() else onStart()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timerState.status == TimerStatus.RUNNING) CrimsonAccent else accentColor
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(76.dp)
                    .testTag("timer_start_pause_button")
            ) {
                Icon(
                    imageVector = if (timerState.status == TimerStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (timerState.status == TimerStatus.RUNNING) "إيقاف مؤقت" else "بدء",
                    tint = Color.Black,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Add 1 Minute Button
            OutlinedButton(
                onClick = onAddMinute,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("timer_add_minute_button")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+1",
                        color = accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "دقيقة",
                        color = TextSecondaryDark,
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Presets
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
        ) {
            Text(
                text = "المؤقتات السريعة الجاهزة",
                color = TextPrimaryDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timerPresets.forEach { preset ->
                    val isCurrent = timerState.totalSeconds == preset.seconds && timerState.status == TimerStatus.IDLE
                    FilterChip(
                        selected = isCurrent,
                        onClick = { onSetDuration(preset.seconds) },
                        label = { Text(preset.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor,
                            labelColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("timer_preset_${preset.seconds}")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
