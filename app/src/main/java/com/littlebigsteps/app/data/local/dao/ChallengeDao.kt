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

    @Query("DELETE FROM challenges WHERE mediumType = :mediumType")
    suspend fun deleteByMedium(mediumType: MediumType)
}
