package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.model.WorldCity
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun WorldClockScreen(
    currentCalendar: Calendar,
    cities: List<WorldCity>,
    searchQuery: String,
    accentColor: Color,
    onSearchChange: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localTz = remember { TimeZone.getDefault() }

    val filteredCities = remember(cities, searchQuery) {
        val list = if (searchQuery.isBlank()) {
            cities
        } else {
            cities.filter {
                it.nameArabic.contains(searchQuery, ignoreCase = true) ||
                        it.countryArabic.contains(searchQuery, ignoreCase = true)
            }
        }
        // Favorites first
        list.sortedByDescending { it.isFavorite }
    }

    Column(
        modifier = modifier
            .testTag("world_clock_screen")
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("ابحث عن مدينة أو دولة...", color = TextTertiaryDark, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondaryDark
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح البحث",
                            tint = TextSecondaryDark
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate900,
                unfocusedContainerColor = Slate900,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Slate800,
                cursorColor = accentColor,
                focusedTextColor = TextPrimaryDark,
                unfocusedTextColor = TextPrimaryDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .testTag("world_clock_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Cities List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredCities, key = { it.id }) { city ->
                WorldCityCard(
                    city = city,
                    localTz = localTz,
                    calendar = currentCalendar,
                    accentColor = accentColor,
                    onToggleFavorite = { onToggleFavorite(city.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun WorldCityCard(
    city: WorldCity,
    localTz: TimeZone,
    calendar: Calendar,
    accentColor: Color,
    onToggleFavorite: () -> Unit
) {
    val cityTz = remember(city.timeZoneId) { TimeZone.getTimeZone(city.timeZoneId) }

    // Time calculations
    val cityCalendar = remember(calendar.timeInMillis, city.timeZoneId) {
        val cal = Calendar.getInstance(cityTz)
        cal.timeInMillis = calendar.timeInMillis
        cal
    }

    val hourFormat = remember(city.timeZoneId) {
        val fmt = SimpleDateFormat("hh:mm", Locale.US)
        fmt.timeZone = cityTz
        fmt
    }
    val amPmFormat = remember(city.timeZoneId) {
        val fmt = SimpleDateFormat("a", Locale("ar"))
        fmt.timeZone = cityTz
        fmt
    }

    val timeString = remember(calendar.timeInMillis) { hourFormat.format(cityCalendar.time) }
    val amPmString = remember(calendar.timeInMillis) { amPmFormat.format(cityCalendar.time) }

    // Daytime or Nighttime?
    val hourOfDay = cityCalendar.get(Calendar.HOUR_OF_DAY)
    val isDayTime = hourOfDay in 6..17

    // Time difference relative to local
    val diffMillis = cityTz.getOffset(calendar.timeInMillis) - localTz.getOffset(calendar.timeInMillis)
    val diffHours = diffMillis / (1000 * 60 * 60)
    val diffText = when {
        diffHours == 0 -> "نفس التوقيت المحلي"
        diffHours > 0 -> "+$diffHours ساعة"
        else -> "$diffHours ساعة"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Slate900.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (city.isFavorite) accentColor.copy(alpha = 0.4f) else Slate800
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("world_city_card_${city.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // City details and flag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Flag badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Slate800),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = city.flag, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = city.nameArabic,
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isDayTime) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDayTime) "نهار" else "ليل",
                            tint = if (isDayTime) Color(0xFFFFB300) else Color(0xFF818CF8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${city.countryArabic} • $diffText",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                }
            }

            // Time and Star Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = timeString,
                        color = TextPrimaryDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = amPmString,
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("fav_button_${city.id}")
                ) {
                    Icon(
                        imageVector = if (city.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "تفضيل",
                        tint = if (city.isFavorite) Color(0xFFFFB300) else Slate700,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
