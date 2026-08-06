package com.littlebigsteps.app.data.remote

import com.littlebigsteps.app.data.remote.dto.ManifestDto
import com.littlebigsteps.app.data.remote.dto.MediumContentDto
import retrofit2.http.GET
import retrofit2.http.Path

/** Contrat HTTP vers le contenu JSON statique (voir /content à la racine du repo). */
interface ContentApiService {

    @GET("manifest.json")
    suspend fun getManifest(): ManifestDto

    @GET("{file}")
    suspend fun getMediumContent(@Path("file") file: String): MediumContentDto
}
