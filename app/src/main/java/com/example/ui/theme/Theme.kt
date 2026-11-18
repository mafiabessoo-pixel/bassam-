package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.local.ThemeMode

@Immutable
data class TacticalColors(
    val gold: Color,
    val goldContainer: Color,
    val silver: Color,
    val success: Color,
    val successContainer: Color,
    val cardBorder: Color,
    val elevatedCard: Color,
    val progressTrack: Color,
    val isDark: Boolean,
)

val LocalTacticalColors = staticCompositionLocalOf {
    TacticalColors(
        gold = PremiumGold,
        goldContainer = GoldContainerDark,
        silver = MetallicSilver,
        success = TacticalGreen,
        successContainer = TacticalGreenContainerDark,
        cardBorder = BorderDark,
        elevatedCard = CardElevatedDark,
        progressTrack = Color(0xFF252A36),
        isDark = true,
    )
}

private val TacticalDarkColorScheme = darkColorScheme(
    primary = DeepRed,
    onPrimary = TextWhite,
    primaryContainer = DeepRedContainerDark,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = MetallicSilver,
    onSecondary = MatteBlack,
    secondaryContainer = Color(0xFF282E3A),
    onSecondaryContainer = TextWhite,
    tertiary = PremiumGold,
    onTertiary = MatteBlack,
    tertiaryContainer = GoldContainerDark,
    onTertiaryContainer = Color(0xFFFFE59E),
    background = MatteBlack,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextLightGray,
    surfaceContainerLowest = MatteBlack,
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = CardDark,
    surfaceContainerHigh = CardElevatedDark,
    surfaceContainerHighest = Color(0xFF292E3C),
    outline = BorderDark,
    outlineVariant = SilverBorder,
    error = Color(0xFFF87171),
    onError = MatteBlack,
)

private val TacticalLightColorScheme = lightColorScheme(
    primary = LightPrimaryRed,
    onPrimary = Color.White,
    primaryContainer = LightRedContainer,
    onPrimaryContainer = Color(0xFF7F1D1D),
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextPrimary,
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    tertiaryContainer = LightGoldContainer,
    onTertiaryContainer = Color(0xFF78350F),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerHighest = Color(0xFFD8DEE8),
    outline = LightBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White,
)

val TacticalShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun CODMGrindTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDark) TacticalDarkColorScheme else TacticalLightColorScheme
    val tacticalColors = if (useDark) {
        TacticalColors(
            gold = PremiumGold,
            goldContainer = GoldContainerDark,
            silver = MetallicSilver,
            success = TacticalGreen,
            successContainer = TacticalGreenContainerDark,
            cardBorder = BorderDark,
            elevatedCard = CardElevatedDark,
            progressTrack = Color(0xFF252A36),
            isDark = true,
        )
    } else {
        TacticalColors(
            gold = Color(0xFFD97706),
            goldContainer = LightGoldContainer,
            silver = Color(0xFF64748B),
            success = Color(0xFF16A34A),
            successContainer = LightGreenContainer,
            cardBorder = LightBorder,
            elevatedCard = Color(0xFFF8FAFC),
            progressTrack = Color(0xFFE2E8F0),
            isDark = false,
        )
    }

    CompositionLocalProvider(LocalTacticalColors provides tacticalColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = TacticalShapes,
            content = content,
        )
    }
}
