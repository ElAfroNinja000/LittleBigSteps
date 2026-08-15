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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.R
import com.littlebigsteps.app.ui.common.findActivity
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
        Text(stringResource(R.string.premium_title), style = MaterialTheme.typography.headlineSmall)

        when {
            state.isPremium -> Text(
                stringResource(R.string.premium_already),
                style = MaterialTheme.typography.bodyLarge
            )
            state.isLoading -> Text(stringResource(R.string.premium_loading), style = MaterialTheme.typography.bodyLarge)
            state.productDetails == null -> Text(
                stringResource(R.string.premium_unavailable),
                style = MaterialTheme.typography.bodyLarge
            )
            // L'offre est un moment d'emphase, comme l'accueil : blanc chaud,
            // mise en avant par le bouton mint plutôt que par un aplat sombre.
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
                        stringResource(R.string.premium_pitch),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        Text(stringResource(R.string.premium_subscribe), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        OutlinedButton(onClick = onBack, shape = PillShape) { Text(stringResource(R.string.action_back)) }
    }
}
