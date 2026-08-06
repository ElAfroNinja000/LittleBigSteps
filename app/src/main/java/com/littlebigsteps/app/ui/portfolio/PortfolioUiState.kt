package com.littlebigsteps.app.ui.portfolio

import com.littlebigsteps.app.data.local.entity.PortfolioEntryEntity
import com.littlebigsteps.app.domain.model.MediumType
import com.littlebigsteps.app.ui.common.toLocalDate
import kotlinx.datetime.LocalDate

data class PortfolioUiState(
    val allEntries: List<PortfolioEntryEntity> = emptyList(),
    /** null = tous les médiums confondus. */
    val mediumFilter: MediumType? = null,
    /** null = toutes les dates confondues. */
    val dateFilter: LocalDate? = null,
    val isLoading: Boolean = true
) {
    val filteredEntries: List<PortfolioEntryEntity>
        get() = allEntries.filter { entry ->
            (mediumFilter == null || entry.completion.mediumType == mediumFilter) &&
                (dateFilter == null || entry.completion.completedAt.toLocalDate() == dateFilter)
        }

    val availableMediums: List<MediumType>
        get() = allEntries.map { it.completion.mediumType }.distinct()

    val availableDates: List<LocalDate>
        get() = allEntries.map { it.completion.completedAt.toLocalDate() }.distinct().sortedDescending()
}
