package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.ContentManifestEntity

@Dao
interface ContentManifestDao {

    @Upsert
    suspend fun upsert(manifest: ContentManifestEntity)

    @Query("SELECT * FROM content_manifest WHERE id = ${ContentManifestEntity.SINGLETON_ID}")
    suspend fun get(): ContentManifestEntity?
}
