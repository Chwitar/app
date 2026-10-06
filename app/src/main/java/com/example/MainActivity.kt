package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainClockScreen
import com.example.ui.screens.NightstandScreen
import com.example.ui.screens.StopwatchScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.WorldClockScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.ClockViewModel

enum class ClockNavTab(
    val titleArabic: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CLOCK("الساعة", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    WORLD("العالم", Icons.Filled.Public, Icons.Outlined.Public),
    STOPWATCH("ساعة إيقاف", Icons.Filled.Timer, Icons.Outlined.Timer),
    TIMER("المؤقت", Icons.Filled.HourglassEmpty, Icons.Outlined.HourglassEmpty)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ClockViewModel = viewModel()
            val clockState by viewModel.clockState.collectAsStateWithLifecycle()
            val worldClockState by viewModel.worldClockState.collectAsStateWithLifecycle()
            val stopwatchState by viewModel.stopwatchState.collectAsStateWithLifecycle()
            val timerState by viewModel.timerState.collectAsStateWithLifecycle()

            MyApplicationTheme(accentColor = clockState.selectedTheme.color) {
                if (clockState.isNightstandMode) {
                    NightstandScreen(
                        uiState = clockState,
                        onClose = { viewModel.setNightstandMode(false) },
                        onBrightnessChange = { viewModel.setNightstandBrightness(it) },
                        onSetTheme = { viewModel.setTheme(it) }
                    )
                } else {
                    ClockMainAppContent(
                        clockState = clockState,
                        worldClockState = worldClockState,
                        stopwatchState = stopwatchState,
                        timerState = timerState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockMainAppContent(
    clockState: com.example.viewmodel.ClockUiState,
    worldClockState: com.example.viewmodel.WorldClockUiState,
    stopwatchState: com.example.viewmodel.StopwatchUiState,
    timerState: com.example.viewmodel.TimerUiState,
    viewModel: ClockViewModel
) {
    var selectedTab by rememberSaveable { mutableStateOf(ClockNavTab.CLOCK) }
    val accentColor = clockState.selectedTheme.color

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedTab.titleArabic,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setNightstandMode(true) },
                        modifier = Modifier.testTag("top_bar_nightstand_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "الوضع الليلي",
                            tint = accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("clock_bottom_navigation")
            ) {
                ClockNavTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.titleArabic,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.titleArabic,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = accentColor,
                            indicatorColor = accentColor,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_tab_animation"
            ) { tab ->
                when (tab) {
                    ClockNavTab.CLOCK -> {
                        MainClockScreen(
                            uiState = clockState,
                            onSetDisplayMode = { viewModel.setDisplayMode(it) },
                            onToggle24Hour = { viewModel.toggle24HourFormat() },
                            onToggleSeconds = { viewModel.toggleShowSeconds() },
                            onSetTheme = { viewModel.setTheme(it) },
                            onOpenNightstand = { viewModel.setNightstandMode(true) }
                        )
                    }
                    ClockNavTab.WORLD -> {
                        WorldClockScreen(
                            currentCalendar = clockState.currentCalendar,
                            cities = worldClockState.cities,
                            searchQuery = worldClockState.searchQuery,
                            accentColor = accentColor,
                            onSearchChange = { viewModel.updateWorldSearch(it) },
                            onToggleFavorite = { viewModel.toggleFavoriteCity(it) }
                        )
                    }
                    ClockNavTab.STOPWATCH -> {
                        StopwatchScreen(
                            stopwatchState = stopwatchState,
                            accentColor = accentColor,
                            onStart = { viewModel.startStopwatch() },
                            onPause = { viewModel.pauseStopwatch() },
                            onReset = { viewModel.resetStopwatch() },
                            onLap = { viewModel.recordLap() }
                        )
                    }
                    ClockNavTab.TIMER -> {
                        TimerScreen(
                            timerState = timerState,
                            accentColor = accentColor,
                            onStart = { viewModel.startTimer() },
                            onPause = { viewModel.pauseTimer() },
                            onReset = { viewModel.resetTimer() },
                            onSetDuration = { viewModel.setTimerDuration(it) },
                            onAddMinute = { viewModel.addMinuteToTimer() }
                        )
                    }
                }
            }
        }
    }
}
