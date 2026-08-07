package com.littlebigsteps.app.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Hébergement statique du contenu (voir CLAUDE.md §10 et /content à la racine
 * du repo pour la structure attendue). Sert directement /content depuis le
 * repo GitHub via raw.githubusercontent.com (fronté par le CDN Fastly de
 * GitHub) — aucune infra supplémentaire à ce stade. Pointe sur `master` : tout
 * commit dans /content est donc immédiatement visible par l'app, cohérent avec
 * la synchro incrémentale (ContentSyncRepository). À remplacer par un vrai
 * bucket + CDN (ou GitHub Pages dédié) si ce couplage direct au repo de code
 * devient gênant en production.
 */
object NetworkConfig {

    const val CONTENT_BASE_URL =
        "https://raw.githubusercontent.com/ElAfroNinja000/LittleBigSteps/master/content/"

    private val json = Json { ignoreUnknownKeys = true }

    fun buildRetrofit(baseUrl: String = CONTENT_BASE_URL): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
