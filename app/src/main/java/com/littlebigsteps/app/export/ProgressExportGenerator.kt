package com.littlebigsteps.app.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Génère localement un résumé de progression en image PNG, PDF, ou format
 * "story" enrichi exclusif premium (Canvas/Bitmap natif, pas de dépendance
 * externe — CLAUDE.md §10) et renvoie un `content://` URI partageable via
 * FileProvider, pour un export/partage autonome (§6).
 */
interface ProgressExportGenerator {
    fun exportAsImage(data: ExportData): Uri
    fun exportAsPdf(data: ExportData): Uri

    /** Format réseaux sociaux enrichi (§7) — l'appelant doit vérifier
     *  data.isPremium avant d'appeler, ce générateur ne le fait pas lui-même. */
    fun exportAsStory(data: ExportData): Uri
}

class CanvasProgressExportGenerator(
    private val context: Context
) : ProgressExportGenerator {

    override fun exportAsImage(data: ExportData): Uri {
        val bitmap = Bitmap.createBitmap(ExportRenderer.WIDTH, ExportRenderer.HEIGHT, Bitmap.Config.ARGB_8888)
        ExportRenderer.draw(Canvas(bitmap), data)

        val file = newExportFile("png")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        bitmap.recycle()
        return fileToUri(file)
    }

    override fun exportAsPdf(data: ExportData): Uri {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(ExportRenderer.WIDTH, ExportRenderer.HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        ExportRenderer.draw(page.canvas, data)
        document.finishPage(page)

        val file = newExportFile("pdf")
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        return fileToUri(file)
    }

    override fun exportAsStory(data: ExportData): Uri {
        val bitmap = Bitmap.createBitmap(
            ExportRenderer.STORY_WIDTH,
            ExportRenderer.STORY_HEIGHT,
            Bitmap.Config.ARGB_8888
        )
        ExportRenderer.drawStory(Canvas(bitmap), data)

        val file = newExportFile("png", baseName = "story")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        bitmap.recycle()
        return fileToUri(file)
    }

    /** Toujours le même nom par type : chaque export écrase le précédent, pas
     *  besoin d'accumuler des fichiers dans le cache. */
    private fun newExportFile(extension: String, baseName: String = "progression"): File {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        return File(exportsDir, "$baseName.$extension")
    }

    private fun fileToUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
