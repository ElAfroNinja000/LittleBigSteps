package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.littlebigsteps.app.data.local.entity.CompletedChallengeEntity
import com.littlebigsteps.app.data.local.entity.PortfolioEntryEntity
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedChallengeDao {

    @Insert
    suspend fun insert(completedChallenge: CompletedChallengeEntity): Long

    /** Portfolio complet, le plus récent en premier, avec le titre et la
     *  description du défi (jointure : challengeId peut être null si le défi
     *  a été retiré du catalogue). */
    @Query(
        "SELECT cc.*, c.title AS challengeTitle, c.description AS challengeDescription " +
            "FROM completed_challenges cc " +
            "LEFT JOIN challenges c ON cc.challengeId = c.id " +
            "ORDER BY cc.completedAt DESC"
    )
    fun observeAll(): Flow<List<PortfolioEntryEntity>>

    @Query(
        "SELECT cc.*, c.title AS challengeTitle, c.description AS challengeDescription " +
            "FROM completed_challenges cc " +
            "LEFT JOIN challenges c ON cc.challengeId = c.id " +
            "WHERE cc.mediumType = :mediumType " +
            "ORDER BY cc.completedAt DESC"
    )
    fun observeByMedium(mediumType: MediumType): Flow<List<PortfolioEntryEntity>>

    @Query(
        "SELECT * FROM completed_challenges " +
            "WHERE souvenirPhotoPath IS NOT NULL OR souvenirNote IS NOT NULL " +
            "ORDER BY completedAt DESC"
    )
    fun observeWithSouvenirs(): Flow<List<CompletedChallengeEntity>>

    /** Réinitialisation manuelle depuis les Paramètres — vide le Portfolio
     *  (voir ChallengeRepositoryImpl.clearHistory). Ne supprime pas les fichiers
     *  photo eux-mêmes (SouvenirPhotoStore), laissés orphelins dans le stockage
     *  interne : acceptable, pas de nettoyage au stade MVP. */
    @Query("DELETE FROM completed_challenges")
    suspend fun deleteAll()
}
