package com.littlebigsteps.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette de l'app. Direction artistique : aplats pastel sans ombre ni
 * bordure, fond blanc chaud partout (y compris les moments d'emphase —
 * accueil, offre premium), mint en couleur principale (actions, états
 * sélectionnés, contours d'accent). Thème clair uniquement (voir Theme.kt) :
 * pas de palette sombre pour l'instant.
 *
 * Les quatre pastels catégoriels servent uniquement à distinguer les médiums
 * (voir [mediumColors]) — le mint n'en fait pas partie pour rester lisible
 * comme "couleur d'action" partout ailleurs.
 */

// --- Couleur principale ---
val Mint = Color(0xFFC0FCD2)
val MintLight = Color(0xFFDFFCE9)
/** Texte/icônes posés sur un aplat mint. */
val MintInk = Color(0xFF14281C)

// --- Neutres chauds ---
val WarmBackground = Color(0xFFFBF9F6)
val WarmSurface = Color(0xFFFFFFFF)
val WarmSurfaceVariant = Color(0xFFF1EEE9)
val WarmOutline = Color(0xFFDDD8D1)
val WarmOutlineVariant = Color(0xFFEBE7E1)

// --- Texte ---
val InkPrimary = Color(0xFF1D2433)
val InkSecondary = Color(0xFF5C6472)

// --- Pastels catégoriels (un par médium, voir MediumColors.kt) ---
val PastelBlue = Color(0xFFDDE7F5)
val PastelBlueInk = Color(0xFF1B3A5C)
val PastelLavender = Color(0xFFE5DEF8)
val PastelLavenderInk = Color(0xFF382A6B)
val PastelPink = Color(0xFFF8DCDC)
val PastelPinkInk = Color(0xFF6B2A2A)
val PastelAmber = Color(0xFFF8ECD5)
val PastelAmberInk = Color(0xFF5C4318)

val ErrorRed = Color(0xFFB3261E)
