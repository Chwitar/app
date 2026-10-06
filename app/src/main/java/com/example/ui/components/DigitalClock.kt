package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.util.Calendar
import java.util.Locale

@Composable
fun DigitalClock(
    calendar: Calendar,
    is24Hour: Boolean,
    showSeconds: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isNightstand: Boolean = false
) {
    val hourRaw = calendar.get(if (is24Hour) Calendar.HOUR_OF_DAY else Calendar.HOUR)
    val hour = if (!is24Hour && hourRaw == 0) 12 else hourRaw
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    val isPm = calendar.get(Calendar.AM_PM) == Calendar.PM

    val hourStr = String.format(Locale.US, "%02d", hour)
    val minuteStr = String.format(Locale.US, "%02d", minute)
    val secondStr = String.format(Locale.US, "%02d", second)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colon"
    )

    Column(
        modifier = modifier
            .testTag("digital_clock_display")
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Time Display Card
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = if (isNightstand) Color.Transparent else Slate900.copy(alpha = 0.85f),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .then(
                    if (!isNightstand) {
                        Modifier
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(accentColor.copy(alpha = 0.45f), Slate800)
                                ),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = accentColor.copy(alpha = 0.25f))
                    } else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = if (isNightstand) 8.dp else 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Hour Box
                    TimeDigitUnit(
                        text = hourStr,
                        textColor = TextPrimaryDark,
                        isNightstand = isNightstand
                    )

                    // Blinking Separator
                    Text(
                        text = ":",
                        color = accentColor.copy(alpha = colonAlpha),
                        fontSize = if (isNightstand) 68.sp else 54.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Minute Box
                    TimeDigitUnit(
                        text = minuteStr,
                        textColor = TextPrimaryDark,
                        isNightstand = isNightstand
                    )

                    // Seconds indicator if enabled
                    AnimatedVisibility(
                        visible = showSeconds,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ":",
                                color = accentColor.copy(alpha = colonAlpha),
                                fontSize = if (isNightstand) 40.sp else 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = secondStr,
                                    color = accentColor,
                                    fontSize = if (isNightstand) 36.sp else 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // AM / PM and 24H Tag
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!is24Hour) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPm) accentColor.copy(alpha = 0.2f) else Slate800)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPm) "مساءً (م)" else "صباحاً (ص)",
                                color = if (isPm) accentColor else TextSecondaryDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate800)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "نظام 24 ساعة",
                                color = TextSecondaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Linear second progress bar inside the card
                    if (showSeconds && !isNightstand) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .width(120.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Slate800)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(second / 59f)
                                    .height(4.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(accentColor.copy(alpha = 0.5f), accentColor)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeDigitUnit(
    text: String,
    textColor: Color,
    isNightstand: Boolean
) {
    Text(
        text = text,
        color = textColor,
        fontSize = if (isNightstand) 72.sp else 58.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = (-1).sp
    )
}
