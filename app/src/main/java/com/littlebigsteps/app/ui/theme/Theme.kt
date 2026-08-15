package com.littlebigsteps.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Thème de l'app. Volontairement **sans couleur dynamique** (Material You) :
 * l'app a une identité visuelle propre (voir Color.kt), la laisser se faire
 * repeindre par le fond d'écran de l'utilisateur la ferait disparaître sur
 * tous les appareils Android 12+. Thème clair forcé, quel que soit le thème
 * système du téléphone : l'app n'a pas de palette sombre pensée pour elle-même.
 */
private val LightColors = lightColorScheme(
    primary = Mint,
    onPrimary = MintInk,
    primaryContainer = MintLight,
    onPrimaryContainer = MintInk,

    // Surfaces d'emphase (accueil, offre premium) : blanc chaud comme le
    // reste de l'app — la mise en avant se fait par un contour/accent mint
    // (voir ces écrans), pas par un aplat sombre. "secondary" n'est donc pas
    // une seconde couleur d'action, juste le ton neutre de ces surfaces.
    secondary = WarmSurface,
    onSecondary = InkPrimary,
    secondaryContainer = WarmSurface,
    onSecondaryContainer = InkPrimary,

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

@Composable
fun LittleBigStepsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
