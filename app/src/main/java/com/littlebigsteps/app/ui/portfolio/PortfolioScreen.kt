package com.littlebigsteps.app.ui.portfolio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.data.local.entity.PortfolioEntryEntity
import com.littlebigsteps.app.ui.common.LocalPhotoThumbnail
import com.littlebigsteps.app.ui.common.label
import com.littlebigsteps.app.ui.common.toDisplayString
import com.littlebigsteps.app.ui.common.toLocalDate

/** Vue chronologique des défis complétés, filtrable par médium et par date (CLAUDE.md §4). */
@Composable
fun PortfolioScreen(
    factory: PortfolioViewModelFactory,
    modifier: Modifier = Modifier
) {
    val viewModel: PortfolioViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Portfolio", style = MaterialTheme.typography.titleMedium)

        if (state.availableMediums.size > 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.mediumFilter == null,
                        onClick = { viewModel.setMediumFilter(null) },
                        label = { Text("Tous") }
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
                        label = { Text("Toutes les dates") }
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
            state.isLoading -> Text("Chargement…", style = MaterialTheme.typography.bodyLarge)
            filtered.isEmpty() -> Text(
                "Aucun défi complété pour l'instant — ton portfolio se remplira au fil des défis.",
                style = MaterialTheme.typography.bodyLarge
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.completion.id }) { entry ->
                    PortfolioEntryCard(entry)
                }
            }
        }
    }
}

@Composable
private fun PortfolioEntryCard(entry: PortfolioEntryEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                entry.challengeTitle ?: "Défi retiré du catalogue",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "${entry.completion.mediumType.label()} · " +
                    "${entry.completion.completedAt.toLocalDate().toDisplayString()} · " +
                    "+${entry.completion.xpEarned} XP",
                style = MaterialTheme.typography.bodyLarge
            )
            entry.completion.souvenirPhotoPath?.let { path ->
                LocalPhotoThumbnail(path = path)
            }
            entry.completion.souvenirNote?.let { note ->
                Text("« $note »", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
