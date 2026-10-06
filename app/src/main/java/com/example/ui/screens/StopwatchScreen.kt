package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LapItem
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark
import com.example.viewmodel.StopwatchUiState
import java.util.Locale

@Composable
fun StopwatchScreen(
    stopwatchState: StopwatchUiState,
    accentColor: Color,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val millis = stopwatchState.elapsedMillis
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    val hundredths = (millis % 1000) / 10

    val minStr = String.format(Locale.US, "%02d", minutes)
    val secStr = String.format(Locale.US, "%02d", seconds)
    val hundStr = String.format(Locale.US, "%02d", hundredths)

    // Fastest and slowest lap identification
    val laps = stopwatchState.laps
    val fastestLapDuration = remember(laps) {
        if (laps.size >= 2) laps.minOfOrNull { it.lapDurationMs } else null
    }
    val slowestLapDuration = remember(laps) {
        if (laps.size >= 2) laps.maxOfOrNull { it.lapDurationMs } else null
    }

    Column(
        modifier = modifier
            .testTag("stopwatch_screen")
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Stopwatch Circular Dial Display
        Box(
            modifier = Modifier
                .size(260.dp)
                .testTag("stopwatch_dial"),
            contentAlignment = Alignment.Center
        ) {
            // Background track
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = Slate800,
                strokeWidth = 8.dp
            )

            // Animated progress ring based on current second
            val progress = (seconds * 1000 + (millis % 1000)) / 60000f
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = accentColor,
                strokeWidth = 8.dp
            )

            // Digital Readout
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$minStr:$secStr",
                        color = TextPrimaryDark,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = ".$hundStr",
                        color = accentColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (laps.isNotEmpty()) {
                    Text(
                        text = "الدورة ${laps.size}",
                        color = TextSecondaryDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Control Action Buttons
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
                enabled = millis > 0 && !stopwatchState.isRunning,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("stopwatch_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "إعادة ضبط",
                    tint = if (millis > 0 && !stopwatchState.isRunning) TextPrimaryDark else Slate700
                )
            }

            // Main Start / Pause Button
            Button(
                onClick = { if (stopwatchState.isRunning) onPause() else onStart() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (stopwatchState.isRunning) CrimsonAccent else accentColor
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(76.dp)
                    .testTag("stopwatch_start_pause_button")
            ) {
                Icon(
                    imageVector = if (stopwatchState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (stopwatchState.isRunning) "إيقاف مؤقت" else "بدء",
                    tint = Color.Black,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Lap Button
            FilledTonalButton(
                onClick = onLap,
                enabled = stopwatchState.isRunning,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("stopwatch_lap_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "تسجيل دورة",
                    tint = if (stopwatchState.isRunning) accentColor else Slate700
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Laps List
        if (laps.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "الدورة", color = TextTertiaryDark, fontSize = 12.sp)
                Text(text = "وقت الدورة", color = TextTertiaryDark, fontSize = 12.sp)
                Text(text = "الوقت الكلي", color = TextTertiaryDark, fontSize = 12.sp)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(laps, key = { it.lapNumber }) { lap ->
                    val isFastest = lap.lapDurationMs == fastestLapDuration
                    val isSlowest = lap.lapDurationMs == slowestLapDuration

                    LapRow(
                        lap = lap,
                        isFastest = isFastest,
                        isSlowest = isSlowest
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LapRow(
    lap: LapItem,
    isFastest: Boolean,
    isSlowest: Boolean
) {
    val lapMin = (lap.lapDurationMs / 1000) / 60
    val lapSec = (lap.lapDurationMs / 1000) % 60
    val lapHund = (lap.lapDurationMs % 1000) / 10

    val totalMin = (lap.totalDurationMs / 1000) / 60
    val totalSec = (lap.totalDurationMs / 1000) % 60
    val totalHund = (lap.totalDurationMs % 1000) / 10

    val lapTimeStr = String.format(Locale.US, "%02d:%02d.%02d", lapMin, lapSec, lapHund)
    val totalTimeStr = String.format(Locale.US, "%02d:%02d.%02d", totalMin, totalSec, totalHund)

    val itemColor = when {
        isFastest -> EmeraldAccent
        isSlowest -> Color(0xFFFF9100)
        else -> TextPrimaryDark
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Slate900.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${lap.lapNumber}",
                    color = TextSecondaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                if (isFastest) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(الأسرع)",
                        color = EmeraldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isSlowest) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(الأبطأ)",
                        color = Color(0xFFFF9100),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = lapTimeStr,
                color = itemColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = totalTimeStr,
                color = TextSecondaryDark,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
