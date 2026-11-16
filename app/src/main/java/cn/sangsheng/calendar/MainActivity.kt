package cn.sangsheng.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember
import cn.sangsheng.calendar.data.AppSettings
import cn.sangsheng.calendar.data.ThemeMode
import cn.sangsheng.calendar.ui.CalendarApp
import cn.sangsheng.calendar.ui.theme.CalendarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settings = remember { AppSettings.get(this) }
            val dark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CalendarTheme(
                darkTheme = dark,
                dynamicColor = settings.useWallpaperColor,
            ) {
                CalendarApp(settings = settings)
            }
        }
    }
}