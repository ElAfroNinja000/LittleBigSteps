package com.littlebigsteps.app.data.repository

import com.littlebigsteps.app.data.local.AppDatabase
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.MediumContentVersionEntity
import com.littlebigsteps.app.data.remote.ContentApiService
import com.littlebigsteps.app.data.remote.dto.ChallengeDto
import com.littlebigsteps.app.domain.model.ChallengeLevel
import com.littlebigsteps.app.domain.model.MediumType

/**
 * Synchronise le catalogue de défis depuis le contenu JSON distant (voir
 * /content à la racine du repo et docs/data-model.md). Ne retélécharge que les
 * fichiers médium dont la version a changé depuis la dernière synchro
 * (MediumContentVersionEntity), conformément à CLAUDE.md §10.
 */
interface ContentSyncRepository {
    suspend fun syncIfNeeded()
}

class ContentSyncRepositoryImpl(
    private val api: ContentApiService,
    private val database: AppDatabase
) : ContentSyncRepository {

    private val challengeDao get() = database.challengeDao()
    private val versionDao get() = database.mediumContentVersionDao()

    override suspend fun syncIfNeeded() {
        val manifest = api.getManifest()

        manifest.mediums.forEach { entry ->
            val mediumType = entry.id.toMediumTypeOrNull() ?: return@forEach
            val syncedVersion = versionDao.get(mediumType)?.syncedVersion
            if (syncedVersion == entry.version) return@forEach // déjà à jour

            val content = api.getMediumContent(entry.file)
            challengeDao.deleteByMedium(mediumType)
            challengeDao.upsertAll(content.challenges.map { it.toEntity(mediumType) })
            versionDao.upsert(MediumContentVersionEntity(mediumType, entry.version))
        }
    }

    private fun String.toMediumTypeOrNull(): MediumType? =
        MediumType.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }

    private fun ChallengeDto.toEntity(mediumType: MediumType) = ChallengeEntity(
        id = id,
        mediumType = mediumType,
        title = title,
        description = description,
        estimatedMinutes = estimatedMinutes,
        level = level.toChallengeLevelOrDefault(),
        isPremiumOnly = isPremiumOnly,
        tags = tags
    )

    private fun String.toChallengeLevelOrDefault(): ChallengeLevel =
        ChallengeLevel.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
            ?: ChallengeLevel.BEGINNER
}
