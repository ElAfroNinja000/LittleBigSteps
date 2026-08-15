package com.littlebigsteps.app.ui.portfolio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.R
import com.littlebigsteps.app.data.local.entity.PortfolioEntryEntity
import com.littlebigsteps.app.ui.common.LocalPhotoThumbnail
import com.littlebigsteps.app.ui.common.MediumTintedPopup
import com.littlebigsteps.app.ui.common.icon
import com.littlebigsteps.app.ui.common.label
import com.littlebigsteps.app.ui.common.toDisplayString
import com.littlebigsteps.app.ui.common.toLocalDate
import com.littlebigsteps.app.ui.theme.mediumColors

/** Vue chronologique des défis complétés, filtrable par médium et par date (CLAUDE.md §4). */
@Composable
fun PortfolioScreen(
    factory: PortfolioViewModelFactory,
    modifier: Modifier = Modifier
) {
    val viewModel: PortfolioViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()
    var selectedEntry by remember { mutableStateOf<PortfolioEntryEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.portfolio_title), style = MaterialTheme.typography.headlineSmall)

        if (state.availableMediums.size > 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.mediumFilter == null,
                        onClick = { viewModel.setMediumFilter(null) },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                }
                items(state.availableMediums) { medium ->
                    FilterChip(
                        selected = state.mediumFilter == medium,
                        onClick = { viewModel.setMediumFilter(medium) },
                        label = { Text(medium.label()) }
                    )
                }
            }
        }

        if (state.availableDates.size > 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.dateFilter == null,
                        onClick = { viewModel.setDateFilter(null) },
                        label = { Text(stringResource(R.string.filter_all_dates)) }
                    )
                }
                items(state.availableDates) { date ->
                    FilterChip(
                        selected = state.dateFilter == date,
                        onClick = { viewModel.setDateFilter(date) },
                        label = { Text(date.toDisplayString()) }
                    )
                }
            }
        }

        val filtered = state.filteredEntries
        when {
            state.isLoading -> Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodyLarge)
            filtered.isEmpty() -> Text(
                stringResource(R.string.portfolio_empty),
                style = MaterialTheme.typography.bodyLarge
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.completion.id }) { entry ->
                    PortfolioEntryTile(entry, onClick = { selectedEntry = entry })
                }
            }
        }
    }

    selectedEntry?.let { entry ->
        PortfolioEntryDetailDialog(entry = entry, onDismiss = { selectedEntry = null })
    }
}

/** Vignette carrée, même code couleur que la liste d'activités : le
 *  portfolio se parcourt à l'œil, on retrouve ses médiums sans lire les
 *  libellés. Photo si elle existe (recadrée en carré), sinon aplat du
 *  médium avec son icône. Tap -> détail complet (titre, médium, date,
 *  description, photo, commentaire). */
@Composable
private fun PortfolioEntryTile(entry: PortfolioEntryEntity, onClick: () -> Unit) {
    val colors = mediumColors(entry.completion.mediumType)
    val photoPath = entry.completion.souvenirPhotoPath
    val title = entry.challengeTitle ?: stringResource(R.string.challenge_removed_from_catalog)
    Surface(
        shape = MaterialTheme.shapes.small,
        color = colors.container,
        contentColor = colors.onContainer,
        onClick = onClick,
        // Le titre n'est plus affiché visuellement dans la grille (le visuel
        // seul suffit à reconnaître son travail) : la description
        // d'accessibilité le porte quand même, pour les lecteurs d'écran et
        // pour identifier une vignette précise dans les tests E2E.
        modifier = Modifier
            .aspectRatio(1f)
            .semantics { contentDescription = title }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (photoPath != null) {
                LocalPhotoThumbnail(path = photoPath, modifier = Modifier.fillMaxSize())
            } else {
                Icon(entry.completion.mediumType.icon(), contentDescription = null, tint = colors.onContainer)
            }
        }
    }
}

/** Détail complet d'une activité complétée : titre, médium, date, description
 *  du défi, photo (entre la description et le commentaire) et commentaire —
 *  photo et commentaire seulement s'ils existent (souvenir optionnel,
 *  CLAUDE.md §3.3). Même popup blanc chaud que "Mes activités". */
@Composable
private fun PortfolioEntryDetailDialog(entry: PortfolioEntryEntity, onDismiss: () -> Unit) {
    val colors = mediumColors(entry.completion.mediumType)
    MediumTintedPopup(accentColor = colors.onContainer, onDismiss = onDismiss) {
        Text(
            entry.challengeTitle ?: stringResource(R.string.challenge_removed_from_catalog),
            style = MaterialTheme.typography.titleMedium,
            color = colors.onContainer
        )
        Text(
            "${entry.completion.mediumType.label()} · " +
                entry.completion.completedAt.toLocalDate().toDisplayString(),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onContainer
        )
        entry.challengeDescription?.let { description ->
            Text(description, style = MaterialTheme.typography.bodyLarge)
        }
        entry.completion.souvenirPhotoPath?.let { path ->
            LocalPhotoThumbnail(
                path = path,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        entry.completion.souvenirNote?.let { note ->
            Text("« $note »", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
