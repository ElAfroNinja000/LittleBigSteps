package com.littlebigsteps.app.data.local

import android.content.Context
import com.littlebigsteps.app.data.remote.dto.ManifestDto
import com.littlebigsteps.app.data.remote.dto.MediumContentDto
import com.littlebigsteps.app.data.remote.dto.PacksDto
import kotlinx.serialization.json.Json

/**
 * Catalogue de défis embarqué dans l'APK — les mêmes fichiers que ceux servis par
 * le CDN (/content à la racine du repo, déclaré comme dossier d'assets, voir
 * app/build.gradle.kts), donc une seule source de vérité.
 *
 * Sert de contenu immédiat dans deux cas où attendre le réseau se voit :
 * le tout premier lancement (sans lui, l'app est vide tant que la synchro n'a pas
 * abouti, et le reste indéfiniment hors-ligne) et un changement de langue, les
 * textes des activités venant du catalogue et non des ressources Android.
 *
 * Toute lecture qui échoue renvoie null plutôt que de lever : un asset absent ou
 * illisible ne doit jamais empêcher l'app de démarrer, la synchro réseau prenant
 * alors le relais (voir ContentSyncRepository).
 */
class BundledContentSource(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun manifest(locale: String): ManifestDto? =
        read("$locale/$MANIFEST_FILE") { json.decodeFromString<ManifestDto>(it) }

    fun mediumContent(locale: String, file: String): MediumContentDto? =
        read("$locale/$file") { json.decodeFromString<MediumContentDto>(it) }

    fun packs(locale: String): PacksDto? =
        read("$locale/$PACKS_FILE") { json.decodeFromString<PacksDto>(it) }

    private fun <T> read(path: String, parse: (String) -> T): T? = runCatching {
        parse(context.assets.open(path).use { it.readBytes().decodeToString() })
    }.getOrNull()

    private companion object {
        const val MANIFEST_FILE = "manifest.json"
        const val PACKS_FILE = "packs.json"
    }
}
