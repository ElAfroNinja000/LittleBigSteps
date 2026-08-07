package com.littlebigsteps.app.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.domain.GamificationRules
import com.littlebigsteps.app.ui.common.label

/** Streak global + XP/niveau par médium (CLAUDE.md §4). */
@Composable
fun ProgressScreen(
    factory: ProgressViewModelFactory,
    modifier: Modifier = Modifier
) {
    val viewModel: ProgressViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) return

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Progression", style = MaterialTheme.typography.titleMedium) }
        item {
            StreakCard(
                currentStreak = state.currentStreak,
                longestStreak = state.longestStreak,
                totalChallengesCompleted = state.totalChallengesCompleted
            )
        }
        items(state.mediumProgress, key = { it.mediumType }) { progress ->
            MediumProgressCard(progress)
        }
    }
}

@Composable
private fun StreakCard(currentStreak: Int, longestStreak: Int, totalChallengesCompleted: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null)
                Text(
                    if (currentStreak > 0) "$currentStreak jour(s) de suite" else "Pas de série en cours",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            // Pas de message culpabilisant en cas de série rompue (CLAUDE.md §4) :
            // juste le compteur actuel, le record, et le total cumulé.
            Text("Record : $longestStreak jour(s)", style = MaterialTheme.typography.bodyLarge)
            Text("$totalChallengesCompleted défis complétés au total", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun MediumProgressCard(progress: MediumProgressEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(progress.mediumType.label(), style = MaterialTheme.typography.titleMedium)
                if (progress.isUnlocked) {
                    Text("Niveau ${progress.level}", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Icon(Icons.Filled.Lock, contentDescription = "Verrouillé (premium)")
                }
            }
            if (progress.isUnlocked) {
                LinearProgressIndicator(
                    progress = { GamificationRules.progressToNextLevel(progress.xp) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "${progress.xp} XP · ${progress.challengesCompletedCount} défi(s) complété(s)",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text("À débloquer avec premium", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
