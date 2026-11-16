package cn.sangsheng.calendar.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class WeekStart { MONDAY, SUNDAY }

/**
 * 应用设置。用 Compose 的 mutableStateOf 承载，
 * 改动立刻触发重组（不需要 Flow/ViewModel）。
 */
class AppSettings private constructor(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)!!) }
            .getOrDefault(ThemeMode.SYSTEM)
    )
        private set

    var useWallpaperColor by mutableStateOf(prefs.getBoolean(KEY_WALLPAPER, true))
        private set

    var weekStart by mutableStateOf(
        runCatching { WeekStart.valueOf(prefs.getString(KEY_WEEK_START, WeekStart.MONDAY.name)!!) }
            .getOrDefault(WeekStart.MONDAY)
    )
        private set

    var showLunar by mutableStateOf(prefs.getBoolean(KEY_SHOW_LUNAR, true))
        private set

    var showFestival by mutableStateOf(prefs.getBoolean(KEY_SHOW_FESTIVAL, true))
        private set

    fun updateThemeMode(value: ThemeMode) {
        themeMode = value
        prefs.edit().putString(KEY_THEME, value.name).apply()
    }

    fun updateWallpaperColor(value: Boolean) {
        useWallpaperColor = value
        prefs.edit().putBoolean(KEY_WALLPAPER, value).apply()
    }

    fun updateWeekStart(value: WeekStart) {
        weekStart = value
        prefs.edit().putString(KEY_WEEK_START, value.name).apply()
    }

    fun updateShowLunar(value: Boolean) {
        showLunar = value
        prefs.edit().putBoolean(KEY_SHOW_LUNAR, value).apply()
    }

    fun updateShowFestival(value: Boolean) {
        showFestival = value
        prefs.edit().putBoolean(KEY_SHOW_FESTIVAL, value).apply()
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_WALLPAPER = "use_wallpaper_color"
        private const val KEY_WEEK_START = "week_start"
        private const val KEY_SHOW_LUNAR = "show_lunar"
        private const val KEY_SHOW_FESTIVAL = "show_festival"

        @Volatile
        private var instance: AppSettings? = null

        fun get(context: Context): AppSettings =
            instance ?: synchronized(this) {
                instance ?: AppSettings(context).also { instance = it }
            }
    }
}