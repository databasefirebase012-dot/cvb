package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppAccentColor(val displayName: String, val primaryColor: Color, val hexCode: String) {
    CYAN("Cyan Neon", Color(0xFF06B6D4), "#06B6D4"),
    RED("Crimson Red", Color(0xFFEF4444), "#EF4444"),
    GREEN("Cyber Green", Color(0xFF22C55E), "#22C55E"),
    YELLOW("Voltage Gold", Color(0xFFEAB308), "#EAB308"),
    PURPLE("Plasma Violet", Color(0xFFA855F7), "#A855F7"),
    ALL_RAINBOW("Rainbow Cyber", Color(0xFF3B82F6), "#3B82F6")
}

enum class TargetGame(val title: String, val packageName: String) {
    FREE_FIRE_ORI("Free Fire ORI", "com.dts.freefireth"),
    FREE_FIRE_MAX("FF MAX", "com.dts.freefiremax")
}

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("xrans_fl_settings", Context.MODE_PRIVATE)

    private val _accentColor = MutableStateFlow(getSavedAccentColor())
    val accentColor: StateFlow<AppAccentColor> = _accentColor.asStateFlow()

    private val _targetGame = MutableStateFlow(getSavedTargetGame())
    val targetGame: StateFlow<TargetGame> = _targetGame.asStateFlow()

    private val _holdDelayMs = MutableStateFlow(prefs.getInt(KEY_HOLD_DELAY, 800))
    val holdDelayMs: StateFlow<Int> = _holdDelayMs.asStateFlow()

    private val _timerDurationSeconds = MutableStateFlow(prefs.getInt(KEY_TIMER_DURATION, 10000))
    val timerDurationSeconds: StateFlow<Int> = _timerDurationSeconds.asStateFlow()

    fun setAccentColor(color: AppAccentColor) {
        prefs.edit().putString(KEY_ACCENT_COLOR, color.name).apply()
        _accentColor.value = color
    }

    private fun getSavedAccentColor(): AppAccentColor {
        val name = prefs.getString(KEY_ACCENT_COLOR, AppAccentColor.CYAN.name)
        return try {
            AppAccentColor.valueOf(name ?: AppAccentColor.CYAN.name)
        } catch (e: Exception) {
            AppAccentColor.CYAN
        }
    }

    fun setTargetGame(game: TargetGame) {
        prefs.edit().putString(KEY_TARGET_GAME, game.name).apply()
        _targetGame.value = game
    }

    private fun getSavedTargetGame(): TargetGame {
        val name = prefs.getString(KEY_TARGET_GAME, TargetGame.FREE_FIRE_ORI.name)
        return try {
            TargetGame.valueOf(name ?: TargetGame.FREE_FIRE_ORI.name)
        } catch (e: Exception) {
            TargetGame.FREE_FIRE_ORI
        }
    }

    fun setHoldDelayMs(delayMs: Int) {
        prefs.edit().putInt(KEY_HOLD_DELAY, delayMs).apply()
        _holdDelayMs.value = delayMs
    }

    fun setTimerDurationSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_TIMER_DURATION, seconds).apply()
        _timerDurationSeconds.value = seconds
    }

    companion object {
        private const val KEY_ACCENT_COLOR = "key_accent_color"
        private const val KEY_TARGET_GAME = "key_target_game"
        private const val KEY_HOLD_DELAY = "key_hold_delay"
        private const val KEY_TIMER_DURATION = "key_timer_duration"
    }
}
