package com.example.tiktak.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Цвета для светлой темы (оранжевый вместо фиолетового)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF6B00), 
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE0B2), 
    onPrimaryContainer = Color(0xFF663C00),
    secondary = Color(0xFFFF9800),      
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF663C00),
    tertiary = Color(0xFFFF5722),       
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFCCBC),
    onTertiaryContainer = Color(0xFF4A1A00),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5E6D3),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

// Цвета для темной темы (оранжевый вместо фиолетового)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF8C42),     
    onPrimary = Color(0xFF4A2A00),
    primaryContainer = Color(0xFF663C00),
    onPrimaryContainer = Color(0xFFFFE0B2),
    secondary = Color(0xFFFFB74D),      
    onSecondary = Color(0xFF4A2A00),
    secondaryContainer = Color(0xFF8C5200),
    onSecondaryContainer = Color(0xFFFFE0B2),
    tertiary = Color(0xFFFF7043),       
    onTertiary = Color(0xFF4A1A00),
    tertiaryContainer = Color(0xFF8C3A00),
    onTertiaryContainer = Color(0xFFFFCCBC),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF3D3525),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

// Цвета для патриотической темы Za наших
private val ZaNashikhLightColorScheme = lightColorScheme(
    primary = Color(0xFFD52B1E),     
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFF6B6B),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF0039A6),      
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E4FF),
    onSecondaryContainer = Color(0xFF001B3E),
    tertiary = Color(0xFFFFD700),          
    onTertiary = Color(0xFF332B00),
    tertiaryContainer = Color(0xFFFFE082),
    onTertiaryContainer = Color(0xFF332B00),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

enum class ThemeType {
    LIGHT,
    DARK,
    SYSTEM,
    ZA_NASHIKH
}

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
                themeType == ThemeType.LIGHT || themeType == ThemeType.ZA_NASHIKH
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
