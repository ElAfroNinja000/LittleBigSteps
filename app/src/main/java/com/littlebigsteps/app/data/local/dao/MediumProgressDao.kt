package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediumProgressDao {

    @Upsert
    suspend fun upsert(progress: MediumProgressEntity)

    @Query("SELECT * FROM medium_progress WHERE mediumType = :mediumType")
    fun observe(mediumType: MediumType): Flow<MediumProgressEntity?>

    @Query("SELECT * FROM medium_progress")
    fun observeAll(): Flow<List<MediumProgressEntity>>

    /** Lecture ponctuelle : le repository lit, recalcule xp/niveau, puis upsert
     *  dans une seule transaction (voir ProgressRepositoryImpl.recordCompletion). */
    @Query("SELECT * FROM medium_progress WHERE mediumType = :mediumType")
    suspend fun getOnce(mediumType: MediumType): MediumProgressEntity?

    /** Réinitialisation manuelle depuis les Paramètres : remet XP/niveau/compteur
     *  à zéro sans toucher isUnlocked (le déblocage dépend du statut premium,
     *  pas de la progression — voir ProgressRepositoryImpl.resetProgress). */
    @Query("UPDATE medium_progress SET xp = 0, level = 1, challengesCompletedCount = 0")
    suspend fun resetAll()
}
