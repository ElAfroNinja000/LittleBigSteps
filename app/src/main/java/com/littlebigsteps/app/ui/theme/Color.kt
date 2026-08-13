package com.littlebigsteps.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette de l'app. Direction artistique : aplats pastel sans ombre ni
 * bordure, fond blanc chaud, navy réservé aux moments d'emphase (accueil,
 * carte streak), mint en couleur principale (actions, états sélectionnés).
 *
 * Les quatre pastels catégoriels servent uniquement à distinguer les médiums
 * (voir [mediumColors]) — le mint n'en fait pas partie pour rester lisible
 * comme "couleur d'action" partout ailleurs.
 */

// --- Couleur principale ---
val Mint = Color(0xFFC0FCD2)
val MintLight = Color(0xFFDFFCE9)
/** Variante du mint pour le thème sombre : plus dense, reste lisible sur navy. */
val MintDeep = Color(0xFFA5E8BE)
/** Texte/icônes posés sur un aplat mint. */
val MintInk = Color(0xFF14281C)

// --- Navy (surfaces d'emphase et fond du thème sombre) ---
val Navy = Color(0xFF232B3B)
val NavyDeep = Color(0xFF131A26)
val NavySurface = Color(0xFF1B2431)

// --- Neutres chauds ---
val WarmBackground = Color(0xFFFBF9F6)
val WarmSurface = Color(0xFFFFFFFF)
val WarmSurfaceVariant = Color(0xFFF1EEE9)
val WarmOutline = Color(0xFFDDD8D1)
val WarmOutlineVariant = Color(0xFFEBE7E1)

val DarkOutline = Color(0xFF3A4454)
val DarkOutlineVariant = Color(0xFF2A3342)

// --- Texte ---
val InkPrimary = Color(0xFF1D2433)
val InkSecondary = Color(0xFF5C6472)
val InkOnDark = Color(0xFFECEFF3)
val InkOnDarkSecondary = Color(0xFFA8B0BE)

// --- Pastels catégoriels (un par médium, voir MediumColors.kt) ---
val PastelBlue = Color(0xFFDDE7F5)
val PastelBlueInk = Color(0xFF1B3A5C)
val PastelLavender = Color(0xFFE5DEF8)
val PastelLavenderInk = Color(0xFF382A6B)
val PastelPink = Color(0xFFF8DCDC)
val PastelPinkInk = Color(0xFF6B2A2A)
val PastelAmber = Color(0xFFF8ECD5)
val PastelAmberInk = Color(0xFF5C4318)

// Versions sombres des pastels : même teinte, assez dense pour porter du
// texte clair sur fond navy.
val PastelBlueDark = Color(0xFF2B4666)
val PastelLavenderDark = Color(0xFF413570)
val PastelPinkDark = Color(0xFF6B3B3B)
val PastelAmberDark = Color(0xFF5E4826)

val ErrorRed = Color(0xFFB3261E)
val ErrorRedLight = Color(0xFFF2B8B5)
