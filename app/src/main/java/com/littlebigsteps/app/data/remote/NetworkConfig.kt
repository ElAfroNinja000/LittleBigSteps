package com.littlebigsteps.app.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Hébergement statique du contenu (voir CLAUDE.md §10 et /content à la racine
 * du repo pour la structure attendue). CONTENT_BASE_URL est un placeholder à
 * remplacer par l'URL CDN réelle une fois le contenu déployé (GitHub Pages,
 * Cloudflare Pages, ou bucket + CDN).
 */
object NetworkConfig {

    const val CONTENT_BASE_URL = "https://TODO-configure-cdn-url.example.com/"

    private val json = Json { ignoreUnknownKeys = true }

    fun buildRetrofit(baseUrl: String = CONTENT_BASE_URL): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
