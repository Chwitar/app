package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClock(
    calendar: Calendar,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    val millis = calendar.get(Calendar.MILLISECOND)

    // Smooth sweeping degrees
    val secondSweep = (second + millis / 1000f) * 6f
    val minuteSweep = (minute + second / 60f) * 6f
    val hourSweep = (hour + minute / 60f + second / 3600f) * 30f

    Box(
        modifier = modifier
            .testTag("analog_clock_dial")
            .sizeIn(minWidth = 240.dp, minHeight = 240.dp, maxWidth = 340.dp, maxHeight = 340.dp)
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // 1. Dial Background with subtle radial gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Slate800.copy(alpha = 0.9f),
                        Slate900,
                        Color(0xFF070B12)
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 2. Outer decorative ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.5f),
                        Slate700,
                        accentColor.copy(alpha = 0.3f),
                        Slate700,
                        accentColor.copy(alpha = 0.5f)
                    ),
                    center = center
                ),
                radius = radius - 4.dp.toPx(),
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // 3. Inner track circle
            drawCircle(
                color = Slate800.copy(alpha = 0.6f),
                radius = radius * 0.85f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 4. Tick marks (60 ticks: 12 bold, 4 cardinals extra bold)
            for (i in 0 until 60) {
                val angleRad = (i * 6 - 90) * (PI / 180.0)
                val isCardinal = i % 15 == 0 // 12, 3, 6, 9
                val isHour = i % 5 == 0

                val tickLength = when {
                    isCardinal -> radius * 0.14f
                    isHour -> radius * 0.10f
                    else -> radius * 0.05f
                }

                val strokeWidth = when {
                    isCardinal -> 3.5.dp.toPx()
                    isHour -> 2.2.dp.toPx()
                    else -> 1.dp.toPx()
                }

                val tickColor = when {
                    isCardinal -> accentColor
                    isHour -> TextPrimaryDark.copy(alpha = 0.8f)
                    else -> TextSecondaryDark.copy(alpha = 0.35f)
                }

                val outerR = radius * 0.88f
                val innerR = outerR - tickLength

                val start = Offset(
                    x = (center.x + innerR * cos(angleRad)).toFloat(),
                    y = (center.y + innerR * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = (center.x + outerR * cos(angleRad)).toFloat(),
                    y = (center.y + outerR * sin(angleRad)).toFloat()
                )

                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 5. Hour Hand
            rotate(degrees = hourSweep, pivot = center) {
                drawHourHand(center = center, radius = radius)
            }

            // 6. Minute Hand
            rotate(degrees = minuteSweep, pivot = center) {
                drawMinuteHand(center = center, radius = radius, accentColor = accentColor)
            }

            // 7. Second Hand (Sweeping smoothly)
            rotate(degrees = secondSweep, pivot = center) {
                drawSecondHand(center = center, radius = radius, accentColor = accentColor)
            }

            // 8. Center Hub
            drawCircle(
                color = accentColor,
                radius = 6.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.Black,
                radius = 2.5.dp.toPx(),
                center = center
            )
        }
    }
}

private fun DrawScope.drawHourHand(center: Offset, radius: Radius) {
    val handLength = radius * 0.52f
    val handWidth = 5.dp.toPx()
    val tailLength = radius * 0.10f

    // Shadow / contrast line
    drawLine(
        color = TextPrimaryDark,
        start = Offset(center.x, center.y + tailLength),
        end = Offset(center.x, center.y - handLength),
        strokeWidth = handWidth,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawMinuteHand(center: Offset, radius: Float, accentColor: Color) {
    val handLength = radius * 0.74f
    val handWidth = 3.5.dp.toPx()
    val tailLength = radius * 0.12f

    drawLine(
        color = Slate700,
        start = Offset(center.x, center.y + tailLength),
        end = Offset(center.x, center.y - handLength),
        strokeWidth = handWidth + 2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = TextPrimaryDark.copy(alpha = 0.95f),
        start = Offset(center.x, center.y + tailLength),
        end = Offset(center.x, center.y - handLength),
        strokeWidth = handWidth,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawSecondHand(center: Offset, radius: Float, accentColor: Color) {
    val handLength = radius * 0.82f
    val tailLength = radius * 0.20f
    val strokeWidth = 1.8.dp.toPx()

    // Second hand line
    drawLine(
        color = accentColor,
        start = Offset(center.x, center.y + tailLength),
        end = Offset(center.x, center.y - handLength),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Tail counter-weight circle
    drawCircle(
        color = accentColor,
        radius = 4.dp.toPx(),
        center = Offset(center.x, center.y + tailLength * 0.7f)
    )
}

private typealias Radius = Float
