package com.littlebigsteps.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Thème de l'app. Volontairement **sans couleur dynamique** (Material You) :
 * l'app a maintenant une identité visuelle propre (voir Color.kt), la laisser
 * se faire repeindre par le fond d'écran de l'utilisateur la ferait disparaître
 * sur tous les appareils Android 12+.
 */
private val LightColors = lightColorScheme(
    primary = Mint,
    onPrimary = MintInk,
    primaryContainer = MintLight,
    onPrimaryContainer = MintInk,

    // Navy en secondaire : c'est la couleur des surfaces d'emphase
    // (accueil, carte de streak), pas une seconde couleur d'action.
    secondary = Navy,
    onSecondary = InkOnDark,
    secondaryContainer = Navy,
    onSecondaryContainer = InkOnDark,

    tertiary = PastelLavender,
    onTertiary = PastelLavenderInk,
    tertiaryContainer = PastelLavender,
    onTertiaryContainer = PastelLavenderInk,

    background = WarmBackground,
    onBackground = InkPrimary,
    surface = WarmSurface,
    onSurface = InkPrimary,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = InkSecondary,

    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant,

    error = ErrorRed,
    onError = WarmSurface
)

private val DarkColors = darkColorScheme(
    primary = MintDeep,
    onPrimary = MintInk,
    primaryContainer = MintInk,
    onPrimaryContainer = MintLight,

    secondary = NavySurface,
    onSecondary = InkOnDark,
    secondaryContainer = NavySurface,
    onSecondaryContainer = InkOnDark,

    tertiary = PastelLavenderDark,
    onTertiary = InkOnDark,
    tertiaryContainer = PastelLavenderDark,
    onTertiaryContainer = InkOnDark,

    background = NavyDeep,
    onBackground = InkOnDark,
    surface = NavySurface,
    onSurface = InkOnDark,
    surfaceVariant = Navy,
    onSurfaceVariant = InkOnDarkSecondary,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,

    error = ErrorRedLight,
    onError = NavyDeep
)

@Composable
fun LittleBigStepsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
