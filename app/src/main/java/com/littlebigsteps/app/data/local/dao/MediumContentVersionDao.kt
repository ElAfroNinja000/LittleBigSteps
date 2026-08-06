package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.MediumContentVersionEntity
import com.littlebigsteps.app.domain.model.MediumType

@Dao
interface MediumContentVersionDao {

    @Upsert
    suspend fun upsert(version: MediumContentVersionEntity)

    @Query("SELECT * FROM medium_content_versions WHERE mediumType = :mediumType")
    suspend fun get(mediumType: MediumType): MediumContentVersionEntity?
}
