package com.littlebigsteps.app.ui.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.ui.common.findActivity
import com.littlebigsteps.app.ui.theme.InkOnDarkSecondary
import com.littlebigsteps.app.ui.theme.PillShape

/**
 * Déblocage premium : tous les médiums + packs thématiques + exports enrichis
 * (CLAUDE.md §7). Le prix vient de Play Billing, jamais codé en dur.
 */
@Composable
fun PremiumScreen(
    factory: PremiumViewModelFactory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: PremiumViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()
    val activity = LocalContext.current.findActivity()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Premium", style = MaterialTheme.typography.headlineSmall)

        when {
            state.isPremium -> Text(
                "Tu es déjà premium — tous les médiums sont débloqués.",
                style = MaterialTheme.typography.bodyLarge
            )
            state.isLoading -> Text("Chargement de l'offre…", style = MaterialTheme.typography.bodyLarge)
            state.productDetails == null -> Text(
                "L'offre premium n'est pas disponible pour l'instant.",
                style = MaterialTheme.typography.bodyLarge
            )
            // L'offre est un moment d'emphase : même traitement navy que
            // l'accueil et la carte de streak.
            else -> Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Débloque tous les médiums, les packs thématiques/saisonniers et " +
                            "les formats d'export enrichis.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = InkOnDarkSecondary
                    )
                    state.priceLabel?.let { price ->
                        Text(price, style = MaterialTheme.typography.headlineSmall)
                    }
                    Button(
                        onClick = { activity?.let(viewModel::purchase) },
                        enabled = activity != null,
                        shape = PillShape,
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("S'abonner", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        OutlinedButton(onClick = onBack, shape = PillShape) { Text("Retour") }
    }
}
