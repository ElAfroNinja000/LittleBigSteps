package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.ChallengePackEntity
import com.littlebigsteps.app.domain.model.MediumType

@Dao
interface ChallengePackDao {

    @Upsert
    suspend fun upsertAll(packs: List<ChallengePackEntity>)

    @Query("SELECT * FROM challenge_packs WHERE mediumType = :mediumType")
    suspend fun getByMedium(mediumType: MediumType): List<ChallengePackEntity>

    @Query("DELETE FROM challenge_packs")
    suspend fun deleteAll()
}
