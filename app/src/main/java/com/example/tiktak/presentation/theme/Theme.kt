package com.example.tiktak.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Цвета для темы "Za наших" (стиль госуслуг России)
private val ZaNashikhLightColorScheme = lightColorScheme(
    primary = Color(0xFF0039A6),      // Синий как у госуслуг
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFFD52B1E),     // Красный как у госуслуг
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),
    tertiary = Color(0xFF005BBB),      // Дополнительный синий
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF005BBB),
    onTertiaryContainer = Color(0xFFFFFFFF),
    error = Color(0xFFD52B1E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

// Стандартная светлая тема
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF03DAC5),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF001F1F),
    tertiary = Color(0xFF3700B3),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD0BCFF),
    onTertiaryContainer = Color(0xFF1D0024),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

// Стандартная темная тема
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFF03DAC5),
    onSecondary = Color(0xFF003734),
    secondaryContainer = Color(0xFF004D45),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFFD0BCFF),
    onTertiary = Color(0xFF57258B),
    tertiaryContainer = Color(0xFF4F378B),
    onTertiaryContainer = Color(0xFFEADDFF),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

enum class ThemeType {
    LIGHT,
    DARK,
    SYSTEM,
    ZA_NASHIKH  // Новая тема "Za наших"
}

// Данные для рекламы ВСРФ
data class VSRFAdvertisement(
    val title: String,
    val message: String,
    val buttonText: String,
    val buttonUrl: String,
    val imageUrl: String? = null
)

@Composable
fun DiaryTheme(
    themeType: ThemeType = ThemeType.SYSTEM,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeType) {
        ThemeType.ZA_NASHIKH -> ZaNashikhLightColorScheme
        ThemeType.LIGHT -> LightColorScheme
        ThemeType.DARK -> DarkColorScheme
        ThemeType.SYSTEM -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                themeType != ThemeType.DARK && themeType != ThemeType.ZA_NASHIKH
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}