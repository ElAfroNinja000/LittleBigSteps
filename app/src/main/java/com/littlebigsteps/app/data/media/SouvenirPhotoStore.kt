package com.littlebigsteps.app.data.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Photos de souvenir stockées dans le stockage interne de l'app, référencées
 * par chemin (CLAUDE.md §10) — aucune synchro cloud, aucune photo hors de
 * l'app sauf celle transitoirement pointée par l'app caméra externe.
 */
interface SouvenirPhotoStore {
    /** Crée un fichier vide + son URI partageable, pour que l'app caméra y écrive. */
    fun createCaptureTarget(): SouvenirPhotoTarget

    /** Copie le contenu d'un URI externe (sélection galerie) en stockage interne. */
    suspend fun importFromUri(sourceUri: Uri): File?

    fun deleteIfExists(path: String)
}

data class SouvenirPhotoTarget(val file: File, val uri: Uri)

class InternalSouvenirPhotoStore(
    private val context: Context
) : SouvenirPhotoStore {

    override fun createCaptureTarget(): SouvenirPhotoTarget {
        val file = newPhotoFile().apply { createNewFile() }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return SouvenirPhotoTarget(file, uri)
    }

    override suspend fun importFromUri(sourceUri: Uri): File? {
        val file = newPhotoFile()
        return try {
            val copied = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
                true
            } ?: false
            if (copied) file else null
        } catch (e: IOException) {
            file.delete()
            null
        }
    }

    override fun deleteIfExists(path: String) {
        File(path).takeIf { it.exists() }?.delete()
    }

    private fun newPhotoFile(): File {
        val dir = File(context.filesDir, "souvenirs").apply { mkdirs() }
        return File(dir, "souvenir_${UUID.randomUUID()}.jpg")
    }
}
