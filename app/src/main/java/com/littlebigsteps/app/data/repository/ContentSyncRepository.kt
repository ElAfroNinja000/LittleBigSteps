package com.littlebigsteps.app.data.repository

import com.littlebigsteps.app.data.local.AppDatabase
import com.littlebigsteps.app.data.local.BundledContentSource
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.ChallengePackEntity
import com.littlebigsteps.app.data.local.entity.ContentManifestEntity
import com.littlebigsteps.app.data.local.entity.MediumContentVersionEntity
import com.littlebigsteps.app.data.remote.ContentApiService
import com.littlebigsteps.app.data.remote.NetworkConfig
import com.littlebigsteps.app.data.remote.dto.ChallengeDto
import com.littlebigsteps.app.data.remote.dto.PackDto
import com.littlebigsteps.app.domain.model.ChallengeLevel
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.datetime.Clock

/**
 * Alimente le catalogue de défis local (voir /content à la racine du repo et
 * docs/data-model.md), depuis deux sources complémentaires :
 *
 * 1. le catalogue **embarqué dans l'APK** (BundledContentSource), appliqué en
 *    premier et sans réseau — c'est lui qui rend le premier lancement et un
 *    changement de langue immédiats ;
 * 2. le **contenu distant** (CDN), qui n'apporte ensuite que les mises à jour
 *    éditoriales publiées depuis la version embarquée.
 *
 * Dans les deux cas, seuls les fichiers dont la version ou la langue a changé
 * sont réappliqués (MediumContentVersionEntity), conformément à CLAUDE.md §10.
 * Les packs thématiques/saisonniers premium (§7) suivent la même logique via
 * ContentManifestEntity.
 */
interface ContentSyncRepository {
    suspend fun syncIfNeeded()
}

class ContentSyncRepositoryImpl(
    private val api: ContentApiService,
    private val database: AppDatabase,
    private val bundled: BundledContentSource
) : ContentSyncRepository {

    private val challengeDao get() = database.challengeDao()
    private val versionDao get() = database.mediumContentVersionDao()
    private val packDao get() = database.challengePackDao()
    private val manifestDao get() = database.contentManifestDao()

    override suspend fun syncIfNeeded() {
        // Langue relue ici, pas au démarrage : elle peut changer en cours de
        // session depuis les Paramètres (voir ContentApiService).
        val locale = NetworkConfig.contentLocale()
        applyBundledContent(locale)
        syncRemoteContent(locale)
    }

    /**
     * Applique le catalogue embarqué pour la langue demandée, uniquement si le
     * contenu local n'est pas déjà dans cette langue. Purement local : quelques
     * millisecondes, aucun réseau, d'où l'effet "instantané" au changement de
     * langue et au premier lancement.
     *
     * La condition porte sur la langue et non sur la version : si le contenu est
     * déjà dans la bonne langue, il peut avoir été mis à jour depuis le CDN et
     * être plus récent que la copie embarquée — le réappliquer serait une
     * régression.
     */
    private suspend fun applyBundledContent(locale: String) {
        val manifest = bundled.manifest(locale) ?: return

        manifest.mediums.forEach { entry ->
            val mediumType = entry.id.toMediumTypeOrNull() ?: return@forEach
            if (versionDao.get(mediumType)?.syncedLocale == locale) return@forEach

            val content = bundled.mediumContent(locale, entry.file) ?: return@forEach
            writeChallenges(mediumType, content.challenges, entry.version, locale)
        }

        if (manifestDao.get()?.syncedLocale != locale) {
            bundled.packs(locale)?.let { writePacks(it.packs, it.version, locale) }
        }
    }

    /** Mises à jour éditoriales publiées depuis la version embarquée. */
    private suspend fun syncRemoteContent(locale: String) {
        val baseUrl = NetworkConfig.contentBaseUrl()
        val manifest = api.getManifest(baseUrl + MANIFEST_FILE)

        manifest.mediums.forEach { entry ->
            val mediumType = entry.id.toMediumTypeOrNull() ?: return@forEach
            val synced = versionDao.get(mediumType)
            // La langue compte autant que la version : les catalogues fr et en
            // portent les mêmes numéros de version, comparer la seule version
            // ferait conclure "déjà à jour" après un changement de langue.
            if (synced?.syncedVersion == entry.version && synced.syncedLocale == locale) return@forEach

            val content = api.getMediumContent(baseUrl + entry.file)
            writeChallenges(mediumType, content.challenges, entry.version, locale)
        }

        syncPacksRemoteIfNeeded(locale, baseUrl)
    }

    /** packs.json est optionnel : son absence (pas encore déployé) ne doit pas
     *  faire échouer la synchro du catalogue de base. */
    private suspend fun syncPacksRemoteIfNeeded(locale: String, baseUrl: String) {
        val packs = runCatching { api.getPacks(baseUrl + PACKS_FILE) }.getOrNull() ?: return
        val synced = manifestDao.get()
        if (synced?.contentVersion == packs.version && synced.syncedLocale == locale) return

        writePacks(packs.packs, packs.version, locale)
    }

    /**
     * Mise à jour en place puis élagage, jamais "tout supprimer puis réinsérer" :
     * les identifiants de défis sont stables d'une version et d'une langue à
     * l'autre, et les supprimer ferait disparaître les activités en cours
     * (CASCADE) et détacherait les entrées du portfolio (SET NULL) à chaque
     * synchro. Voir ChallengeDao.deleteByMediumNotIn.
     */
    private suspend fun writeChallenges(
        mediumType: MediumType,
        dtos: List<ChallengeDto>,
        version: String,
        locale: String
    ) {
        val challenges = dtos.map { it.toEntity(mediumType) }
        if (challenges.isNotEmpty()) {
            challengeDao.upsertAll(challenges)
            challengeDao.deleteByMediumNotIn(mediumType, challenges.map { it.id })
        }
        versionDao.upsert(MediumContentVersionEntity(mediumType, version, locale))
    }

    /** Un pack retiré du catalogue doit disparaître, et rien ne dépend d'un pack
     *  par clé étrangère (packId n'est qu'une colonne de ChallengeEntity) — le
     *  remplacement complet est donc sans effet de bord ici. */
    private suspend fun writePacks(dtos: List<PackDto>, version: String, locale: String) {
        packDao.deleteAll()
        packDao.upsertAll(dtos.mapNotNull { it.toEntity() })
        manifestDao.upsert(
            ContentManifestEntity(
                contentVersion = version,
                syncedLocale = locale,
                lastSyncAt = Clock.System.now()
            )
        )
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
        tags = tags,
        tips = tips,
        packId = packId
    )

    private fun PackDto.toEntity(): ChallengePackEntity? {
        val medium = mediumId.toMediumTypeOrNull() ?: return null
        return ChallengePackEntity(
            id = id,
            mediumType = medium,
            title = title,
            description = description,
            isPremiumOnly = isPremiumOnly
        )
    }

    private fun String.toChallengeLevelOrDefault(): ChallengeLevel =
        ChallengeLevel.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
            ?: ChallengeLevel.BEGINNER

    private companion object {
        const val MANIFEST_FILE = "manifest.json"
        const val PACKS_FILE = "packs.json"
    }
}
