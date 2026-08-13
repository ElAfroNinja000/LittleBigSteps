package com.littlebigsteps.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Rayons généreux, dans l'esprit "aplats doux" de la direction artistique :
 * les cartes d'activité et les tuiles de stats sont franchement arrondies,
 * jamais de coin vif.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Boutons et chips : pilule pleine, signature de la DA. */
val PillShape = RoundedCornerShape(percent = 50)
