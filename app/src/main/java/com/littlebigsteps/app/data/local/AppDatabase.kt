package com.littlebigsteps.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.littlebigsteps.app.data.local.dao.ChallengeDao
import com.littlebigsteps.app.data.local.dao.CompletedChallengeDao
import com.littlebigsteps.app.data.local.dao.ContentManifestDao
import com.littlebigsteps.app.data.local.dao.GlobalProgressDao
import com.littlebigsteps.app.data.local.dao.MediumContentVersionDao
import com.littlebigsteps.app.data.local.dao.MediumProgressDao
import com.littlebigsteps.app.data.local.dao.UserPreferencesDao
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.CompletedChallengeEntity
import com.littlebigsteps.app.data.local.entity.ContentManifestEntity
import com.littlebigsteps.app.data.local.entity.GlobalProgressEntity
import com.littlebigsteps.app.data.local.entity.MediumContentVersionEntity
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.data.local.entity.UserPreferencesEntity

/**
 * Base locale unique de l'app. Aucune synchro cloud : tout ce qui touche
 * l'utilisateur (préférences, progression, souvenirs) vit ici. Le contenu des
 * défis est mis en cache ici aussi (ChallengeEntity), mais sa source de vérité
 * reste le JSON distant (voir data/remote).
 */
@Database(
    entities = [
        ChallengeEntity::class,
        CompletedChallengeEntity::class,
        MediumProgressEntity::class,
        GlobalProgressEntity::class,
        UserPreferencesEntity::class,
        ContentManifestEntity::class,
        MediumContentVersionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun challengeDao(): ChallengeDao
    abstract fun completedChallengeDao(): CompletedChallengeDao
    abstract fun mediumProgressDao(): MediumProgressDao
    abstract fun globalProgressDao(): GlobalProgressDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun contentManifestDao(): ContentManifestDao
    abstract fun mediumContentVersionDao(): MediumContentVersionDao

    companion object {
        private const val DATABASE_NAME = "little_big_steps.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
    }
}
