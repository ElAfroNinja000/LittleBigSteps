package com.littlebigsteps.app.data.remote

import com.littlebigsteps.app.domain.preferredLanguage
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
 *
 * Contenu localisé par sous-dossier (/content/fr, /content/en). La langue suivie
 * est celle de l'app (langue système par défaut, ou choix explicite fait dans les
 * Paramètres), et elle est relue à chaque appel plutôt que figée au démarrage —
 * voir ContentApiService. Le français reste la langue de repli pour toute langue
 * non prise en charge.
 */
object NetworkConfig {

    private const val CONTENT_BASE_URL =
        "https://raw.githubusercontent.com/ElAfroNinja000/LittleBigSteps/master/content/"

    private val SUPPORTED_CONTENT_LOCALES = setOf("fr", "en")
    private const val DEFAULT_CONTENT_LOCALE = "fr"

    private val json = Json { ignoreUnknownKeys = true }

    /** Langue de contenu effectivement servie : la langue demandée si elle est
     *  prise en charge, sinon le repli français (voir SUPPORTED_CONTENT_LOCALES).
     *  Persistée avec le contenu synchronisé pour détecter un changement de
     *  langue (voir MediumContentVersionEntity.syncedLocale). */
    fun contentLocale(language: String = preferredLanguage()): String =
        language.takeIf { it in SUPPORTED_CONTENT_LOCALES } ?: DEFAULT_CONTENT_LOCALE

    /** Dossier de contenu correspondant à la langue courante de l'app. */
    fun contentBaseUrl(language: String = preferredLanguage()): String =
        "$CONTENT_BASE_URL${contentLocale(language)}/"

    // baseUrl requise par Retrofit mais non utilisée : tous les appels passent une
    // URL complète (@Url), voir ContentApiService.
    fun buildRetrofit(baseUrl: String = CONTENT_BASE_URL): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
