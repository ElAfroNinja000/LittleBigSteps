package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {

    /** Insère/remplace en masse lors d'une synchro de contenu (voir ContentManifestEntity). */
    @Upsert
    suspend fun upsertAll(challenges: List<ChallengeEntity>)

    @Query("SELECT * FROM challenges WHERE mediumType = :mediumType ORDER BY id")
    fun observeByMedium(mediumType: MediumType): Flow<List<ChallengeEntity>>

    /** Lecture ponctuelle utilisée pour tirer les 2-3 défis proposés à chaque itération. */
    @Query("SELECT * FROM challenges WHERE mediumType = :mediumType")
    suspend fun getAllByMedium(mediumType: MediumType): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getById(id: String): ChallengeEntity?

    /** Tous les défis d'un pack thématique/saisonnier — pas de tirage
     *  aléatoire ici, l'utilisateur parcourt le pack en entier (CLAUDE.md §7). */
    @Query("SELECT * FROM challenges WHERE packId = :packId ORDER BY id")
    suspend fun getByPackId(packId: String): List<ChallengeEntity>

    /**
     * Retire les défis d'un médium absents du catalogue fraîchement synchronisé.
     *
     * Remplace un "tout supprimer puis tout réinsérer" : les défis conservés ne
     * sont jamais supprimés, donc les suppressions en cascade (activité en cours,
     * voir ChallengeProgressEntity) et les mises à NULL (portfolio, voir
     * CompletedChallengeEntity) ne se déclenchent que pour ce qui a réellement
     * disparu du catalogue — et pas à chaque mise à jour de contenu ou changement
     * de langue, qui réécrit pourtant toutes les lignes.
     *
     * [keepIds] ne doit jamais être vide : `NOT IN ()` est invalide en SQLite
     * (voir ContentSyncRepository, qui n'appelle ceci qu'avec un catalogue non vide).
     */
    @Query("DELETE FROM challenges WHERE mediumType = :mediumType AND id NOT IN (:keepIds)")
    suspend fun deleteByMediumNotIn(mediumType: MediumType, keepIds: List<String>)
}
