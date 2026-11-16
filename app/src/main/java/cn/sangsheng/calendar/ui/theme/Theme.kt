package cn.sangsheng.calendar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * 兜底色板：设备不支持动态取色时使用（API 31+ 实际上总能取到壁纸色）。
 */
private val FallbackLight = lightColorScheme(
    primary = Color(0xFF1B6EF3),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = Color(0xFF565E71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDAE2F9),
    onSecondaryContainer = Color(0xFF131C2B),
    tertiary = Color(0xFF705574),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFAD8FD),
    onTertiaryContainer = Color(0xFF28132E),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F3F7),
    surfaceContainer = Color(0xFFEFEDF1),
    surfaceContainerHigh = Color(0xFFE9E7EC),
    surfaceContainerHighest = Color(0xFFE3E1E6),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
)

private val FallbackDark = darkColorScheme(
    primary = Color(0xFFADC6FF),
    onPrimary = Color(0xFF002E69),
    primaryContainer = Color(0xFF004494),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFFBEC6DC),
    onSecondary = Color(0xFF283141),
    secondaryContainer = Color(0xFF3E4759),
    onSecondaryContainer = Color(0xFFDAE2F9),
    tertiary = Color(0xFFDDBCE0),
    onTertiary = Color(0xFF3F2844),
    tertiaryContainer = Color(0xFF573E5C),
    onTertiaryContainer = Color(0xFFFAD8FD),
    background = Color(0xFF1B1B1F),
    onBackground = Color(0xFFE3E1E6),
    surface = Color(0xFF1B1B1F),
    onSurface = Color(0xFFE3E1E6),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceContainerLowest = Color(0xFF0F0D13),
    surfaceContainerLow = Color(0xFF1B1B1F),
    surfaceContainer = Color(0xFF1F1F23),
    surfaceContainerHigh = Color(0xFF2A2A2E),
    surfaceContainerHighest = Color(0xFF353438),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
)

/**
 * 莫奈取色：Android 12+ 由系统从壁纸推导整套 tonal palette，
 * dynamicColor 打开即为「跟随壁纸」的真 Material You。
 */
@Composable
fun CalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor -> if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> FallbackDark
        else -> FallbackLight
    }
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}