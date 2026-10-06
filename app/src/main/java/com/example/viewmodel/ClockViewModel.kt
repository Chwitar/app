package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AvailableThemes
import com.example.model.ClockDisplayMode
import com.example.model.ClockThemeOption
import com.example.model.LapItem
import com.example.model.TimerStatus
import com.example.model.WorldCity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

data class ClockUiState(
    val currentCalendar: Calendar = Calendar.getInstance(),
    val is24HourFormat: Boolean = false,
    val showSeconds: Boolean = true,
    val showDate: Boolean = true,
    val displayMode: ClockDisplayMode = ClockDisplayMode.DIGITAL,
    val selectedTheme: ClockThemeOption = AvailableThemes[0],
    val isNightstandMode: Boolean = false,
    val nightstandBrightness: Float = 0.85f,
    val nightstandShiftPx: Float = 0f
)

data class WorldClockUiState(
    val cities: List<WorldCity> = defaultWorldCities,
    val searchQuery: String = ""
)

data class StopwatchUiState(
    val elapsedMillis: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<LapItem> = emptyList()
)

data class TimerUiState(
    val totalSeconds: Long = 300L, // 5 min default
    val remainingSeconds: Long = 300L,
    val status: TimerStatus = TimerStatus.IDLE
)

private val defaultWorldCities = listOf(
    WorldCity("makkah", "مكة المكرمة", "المملكة العربية السعودية", "Asia/Riyadh", "🇸🇦", isFavorite = true),
    WorldCity("cairo", "القاهرة", "مصر", "Africa/Cairo", "🇪🇬", isFavorite = true),
    WorldCity("dubai", "دبي", "الإمارات العربية المتحدة", "Asia/Dubai", "🇦🇪", isFavorite = true),
    WorldCity("jerusalem", "القدس الشريف", "فلسطين", "Asia/Jerusalem", "🇵🇸", isFavorite = true),
    WorldCity("baghdad", "بغداد", "العراق", "Asia/Baghdad", "🇮🇶"),
    WorldCity("istanbul", "إسطنبول", "تركيا", "Europe/Istanbul", "🇹🇷"),
    WorldCity("london", "لندن", "المملكة المتحدة", "Europe/London", "🇬🇧", isFavorite = true),
    WorldCity("paris", "باريس", "فرنسا", "Europe/Paris", "🇫🇷"),
    WorldCity("new_york", "نيويورك", "الولايات المتحدة", "America/New_York", "🇺🇸"),
    WorldCity("tokyo", "طوكيو", "اليابان", "Asia/Tokyo", "🇯🇵"),
    WorldCity("sydney", "سيدني", "أستراليا", "Australia/Sydney", "🇦🇺"),
    WorldCity("kuala_lumpur", "كوالالمبور", "ماليزيا", "Asia/Kuala_Lumpur", "🇲🇾")
)

class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val _clockState = MutableStateFlow(ClockUiState())
    val clockState: StateFlow<ClockUiState> = _clockState.asStateFlow()

    private val _worldClockState = MutableStateFlow(WorldClockUiState())
    val worldClockState: StateFlow<WorldClockUiState> = _worldClockState.asStateFlow()

    private val _stopwatchState = MutableStateFlow(StopwatchUiState())
    val stopwatchState: StateFlow<StopwatchUiState> = _stopwatchState.asStateFlow()

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private var clockTickerJob: Job? = null
    private var stopwatchJob: Job? = null
    private var timerJob: Job? = null

    private var lastLapTimestamp: Long = 0L

    init {
        startClockTicker()
    }

    private fun startClockTicker() {
        clockTickerJob?.cancel()
        clockTickerJob = viewModelScope.launch {
            var counter = 0
            while (isActive) {
                _clockState.update { current ->
                    // Every ~30 seconds, slight shift in nightstand mode to prevent burn-in
                    val shift = if (current.isNightstandMode && counter % 600 == 0) {
                        ((counter / 600) % 5 * 4f) - 8f
                    } else current.nightstandShiftPx

                    current.copy(
                        currentCalendar = Calendar.getInstance(),
                        nightstandShiftPx = shift
                    )
                }
                counter++
                delay(50) // 50ms tick allows smooth sweeping second hand & responsive clock
            }
        }
    }

    fun setDisplayMode(mode: ClockDisplayMode) {
        _clockState.update { it.copy(displayMode = mode) }
    }

    fun toggle24HourFormat() {
        _clockState.update { it.copy(is24HourFormat = !it.is24HourFormat) }
    }

    fun toggleShowSeconds() {
        _clockState.update { it.copy(showSeconds = !it.showSeconds) }
    }

    fun setTheme(theme: ClockThemeOption) {
        _clockState.update { it.copy(selectedTheme = theme) }
    }

    fun setNightstandMode(enabled: Boolean) {
        _clockState.update { it.copy(isNightstandMode = enabled) }
    }

    fun setNightstandBrightness(brightness: Float) {
        _clockState.update { it.copy(nightstandBrightness = brightness.coerceIn(0.15f, 1f)) }
    }

    // --- World Clock ---
    fun updateWorldSearch(query: String) {
        _worldClockState.update { it.copy(searchQuery = query) }
    }

    fun toggleFavoriteCity(cityId: String) {
        _worldClockState.update { state ->
            val updated = state.cities.map { city ->
                if (city.id == cityId) city.copy(isFavorite = !city.isFavorite) else city
            }
            state.copy(cities = updated)
        }
    }

    // --- Stopwatch ---
    fun startStopwatch() {
        if (_stopwatchState.value.isRunning) return
        vibrateLight()
        val startTime = System.currentTimeMillis() - _stopwatchState.value.elapsedMillis
        _stopwatchState.update { it.copy(isRunning = true) }

        stopwatchJob?.cancel()
        stopwatchJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                _stopwatchState.update { it.copy(elapsedMillis = now - startTime) }
                delay(16) // ~60fps
            }
        }
    }

    fun pauseStopwatch() {
        vibrateLight()
        stopwatchJob?.cancel()
        _stopwatchState.update { it.copy(isRunning = false) }
    }

    fun resetStopwatch() {
        vibrateLight()
        stopwatchJob?.cancel()
        lastLapTimestamp = 0L
        _stopwatchState.update { StopwatchUiState() }
    }

    fun recordLap() {
        val currentTotal = _stopwatchState.value.elapsedMillis
        if (currentTotal <= 0) return
        vibrateLight()

        val lapDuration = currentTotal - lastLapTimestamp
        lastLapTimestamp = currentTotal

        val newLap = LapItem(
            lapNumber = _stopwatchState.value.laps.size + 1,
            lapDurationMs = lapDuration,
            totalDurationMs = currentTotal
        )

        _stopwatchState.update {
            it.copy(laps = listOf(newLap) + it.laps)
        }
    }

    // --- Timer ---
    fun setTimerDuration(seconds: Long) {
        timerJob?.cancel()
        val duration = seconds.coerceAtLeast(1)
        _timerState.update {
            TimerUiState(
                totalSeconds = duration,
                remainingSeconds = duration,
                status = TimerStatus.IDLE
            )
        }
    }

    fun startTimer() {
        if (_timerState.value.status == TimerStatus.RUNNING) return
        vibrateLight()
        _timerState.update { it.copy(status = TimerStatus.RUNNING) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _timerState.value.remainingSeconds > 0) {
                delay(1000)
                if (_timerState.value.status == TimerStatus.RUNNING) {
                    _timerState.update { current ->
                        val next = current.remainingSeconds - 1
                        if (next <= 0) {
                            current.copy(remainingSeconds = 0, status = TimerStatus.FINISHED)
                        } else {
                            current.copy(remainingSeconds = next)
                        }
                    }
                }
            }
            if (_timerState.value.remainingSeconds <= 0) {
                vibrateFinish()
            }
        }
    }

    fun pauseTimer() {
        vibrateLight()
        _timerState.update { it.copy(status = TimerStatus.PAUSED) }
    }

    fun resetTimer() {
        vibrateLight()
        timerJob?.cancel()
        _timerState.update {
            it.copy(
                remainingSeconds = it.totalSeconds,
                status = TimerStatus.IDLE
            )
        }
    }

    fun addMinuteToTimer() {
        vibrateLight()
        _timerState.update {
            it.copy(
                totalSeconds = it.totalSeconds + 60,
                remainingSeconds = it.remainingSeconds + 60
            )
        }
    }

    private fun vibrateLight() {
        try {
            val vibrator = getVibrator()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateFinish() {
        try {
            val vibrator = getVibrator()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 300, 200, 300, 200, 500)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(1000)
            }
        } catch (_: Exception) {}
    }

    private fun getVibrator(): Vibrator? {
        val context = getApplication<Application>()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onCleared() {
        super.onCleared()
        clockTickerJob?.cancel()
        stopwatchJob?.cancel()
        timerJob?.cancel()
    }
}
