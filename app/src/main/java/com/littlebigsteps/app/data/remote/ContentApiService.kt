package com.littlebigsteps.app.data.remote

import com.littlebigsteps.app.data.remote.dto.ManifestDto
import com.littlebigsteps.app.data.remote.dto.MediumContentDto
import com.littlebigsteps.app.data.remote.dto.PacksDto
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Contrat HTTP vers le contenu JSON statique (voir /content à la racine du repo).
 *
 * URL complète passée à chaque appel (@Url) plutôt que des chemins relatifs à la
 * baseUrl de Retrofit : le dossier de contenu dépend de la langue courante, qui
 * peut changer en cours de session (Paramètres). Avec des chemins relatifs, la
 * langue était figée à la construction de Retrofit et un changement en cours de
 * session continuait de télécharger l'ancienne langue jusqu'au redémarrage du
 * process. Les URL sont construites par ContentSyncRepository via
 * NetworkConfig.contentBaseUrl().
 */
interface ContentApiService {

    @GET
    suspend fun getManifest(@Url url: String): ManifestDto

    @GET
    suspend fun getMediumContent(@Url url: String): MediumContentDto

    /** Packs thématiques/saisonniers premium (CLAUDE.md §7), synchronisés à
     *  part du catalogue de base. */
    @GET
    suspend fun getPacks(@Url url: String): PacksDto
}
