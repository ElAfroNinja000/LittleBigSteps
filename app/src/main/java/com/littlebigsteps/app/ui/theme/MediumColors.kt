package com.littlebigsteps.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.littlebigsteps.app.domain.model.MediumType

/** Aplat pastel + couleur du texte posé dessus, pour un médium donné. */
data class MediumColorPair(val container: Color, val onContainer: Color)

/**
 * Un pastel par médium : c'est ce qui donne à la liste d'activités sa lecture
 * immédiate (on repère le médium à la couleur avant de lire le titre). Les
 * teintes viennent de [Color.kt] et basculent en version dense en thème sombre.
 */
@Composable
@ReadOnlyComposable
fun mediumColors(mediumType: MediumType): MediumColorPair {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when (mediumType) {
        MediumType.PHOTO ->
            if (dark) MediumColorPair(PastelBlueDark, InkOnDark)
            else MediumColorPair(PastelBlue, PastelBlueInk)
        MediumType.DRAWING ->
            if (dark) MediumColorPair(PastelLavenderDark, InkOnDark)
            else MediumColorPair(PastelLavender, PastelLavenderInk)
        MediumType.WRITING ->
            if (dark) MediumColorPair(PastelPinkDark, InkOnDark)
            else MediumColorPair(PastelPink, PastelPinkInk)
        MediumType.CRAFT ->
            if (dark) MediumColorPair(PastelAmberDark, InkOnDark)
            else MediumColorPair(PastelAmber, PastelAmberInk)
    }
}

private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue
