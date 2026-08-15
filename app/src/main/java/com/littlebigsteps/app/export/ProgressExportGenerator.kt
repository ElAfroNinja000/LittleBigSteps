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
 * Génère localement un résumé de progression en image PNG ou PDF (Canvas/Bitmap
 * natif, pas de dépendance externe — CLAUDE.md §10) et renvoie un `content://`
 * URI partageable via FileProvider, pour un export/partage autonome (§6).
 */
interface ProgressExportGenerator {
    fun exportAsImage(data: ExportData): Uri
    fun exportAsPdf(data: ExportData): Uri
}

class CanvasProgressExportGenerator(
    private val context: Context
) : ProgressExportGenerator {

    override fun exportAsImage(data: ExportData): Uri {
        val bitmap = Bitmap.createBitmap(ExportRenderer.WIDTH, ExportRenderer.HEIGHT, Bitmap.Config.ARGB_8888)
        ExportRenderer.draw(Canvas(bitmap), data, context)

        val file = newExportFile("png")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        bitmap.recycle()
        return fileToUri(file)
    }

    override fun exportAsPdf(data: ExportData): Uri {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(ExportRenderer.WIDTH, ExportRenderer.HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        ExportRenderer.draw(page.canvas, data, context)
        document.finishPage(page)

        val file = newExportFile("pdf")
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
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
