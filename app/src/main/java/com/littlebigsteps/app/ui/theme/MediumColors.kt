package com.littlebigsteps.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.littlebigsteps.app.domain.model.MediumType

/** Aplat pastel + couleur du texte posé dessus, pour un médium donné. */
data class MediumColorPair(val container: Color, val onContainer: Color)

/**
 * Un pastel par médium : c'est ce qui donne à la liste d'activités sa lecture
 * immédiate (on repère le médium à la couleur avant de lire le titre). Les
 * teintes viennent de [Color.kt]. Thème clair uniquement (voir Theme.kt) :
 * pas besoin de lire le thème courant, la teinte est fixe.
 */
fun mediumColors(mediumType: MediumType): MediumColorPair = when (mediumType) {
    MediumType.PHOTO -> MediumColorPair(PastelBlue, PastelBlueInk)
    MediumType.DRAWING -> MediumColorPair(PastelLavender, PastelLavenderInk)
    MediumType.WRITING -> MediumColorPair(PastelPink, PastelPinkInk)
    MediumType.CRAFT -> MediumColorPair(PastelAmber, PastelAmberInk)
}
