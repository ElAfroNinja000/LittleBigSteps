package com.littlebigsteps.app.ui.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.R
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.domain.GamificationRules
import com.littlebigsteps.app.ui.common.label
import com.littlebigsteps.app.ui.theme.PillShape
import com.littlebigsteps.app.ui.theme.mediumColors

/** XP/niveau par médium (CLAUDE.md §4) — export retiré de cette vue (partage
 *  jugé hors du cœur de la progression, le code reste disponible ailleurs si
 *  besoin) ; système de badges supprimé entièrement (plus une feature de
 *  l'app). Pas de compteur de streak affiché :
 *  le chiffre "N jours de suite" crée une pression de type "ne pas casser la
 *  chaîne" même sans message culpabilisant explicite, à contre-courant de
 *  CLAUDE.md §4. Le streak reste suivi en base (currentStreak/longestStreak,
 *  ProgressRepository) — c'est un signal de rétention interne (§8), pas
 *  forcément un chiffre à exposer à l'utilisateur. */
@Composable
fun ProgressScreen(
    factory: ProgressViewModelFactory,
    onNavigateToPremium: () -> Unit,
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
        item { Text(stringResource(R.string.progress_title), style = MaterialTheme.typography.titleMedium) }
        item {
            MonthlyStatsRow(
                activitiesThisMonth = state.activitiesThisMonth,
                totalXp = state.totalXp
            )
        }
        // Le médium accessible en free est trié en tête par le ViewModel.
        items(state.mediumProgress, key = { it.mediumType }) { progress ->
            MediumProgressCard(progress)
        }
        if (state.mediumProgress.any { !it.isUnlocked }) {
            item {
                Button(onClick = onNavigateToPremium, shape = PillShape) {
                    Text(stringResource(R.string.pass_to_premium))
                }
            }
        }
    }
}

/** Deux métriques purement cumulatives — jamais de baisse en cours de mois,
 *  contrairement au streak retiré de cet écran (voir doc de ProgressScreen). */
@Composable
private fun MonthlyStatsRow(activitiesThisMonth: Int, totalXp: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(
            value = "$activitiesThisMonth",
            label = stringResource(R.string.stat_activities_this_month),
            modifier = Modifier.weight(1f)
        )
        StatTile(
            value = "$totalXp",
            label = stringResource(R.string.stat_total_xp),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MediumProgressCard(progress: MediumProgressEntity) {
    // Couleur dédiée du médium même verrouillé — le cadenas reste le seul
    // indicateur de verrouillage, la couleur sert à reconnaître le médium
    // d'un coup d'œil avant même de le débloquer.
    val colors = mediumColors(progress.mediumType)
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.container,
        contentColor = colors.onContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(progress.mediumType.label(), style = MaterialTheme.typography.titleMedium)
                if (progress.isUnlocked) {
                    Text(stringResource(R.string.level_label, progress.level), style = MaterialTheme.typography.bodyLarge)
                } else {
                    Icon(Icons.Filled.Lock, contentDescription = stringResource(R.string.locked_premium_description))
                }
            }
            if (progress.isUnlocked) {
                LinearProgressIndicator(
                    progress = { GamificationRules.progressToNextLevel(progress.xp) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    stringResource(R.string.medium_xp_summary, progress.xp, progress.challengesCompletedCount),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(stringResource(R.string.medium_locked_body), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
