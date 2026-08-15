package com.littlebigsteps.app.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.littlebigsteps.app.R
import com.littlebigsteps.app.data.local.entity.GlobalProgressEntity
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.data.local.entity.PortfolioEntryEntity
import com.littlebigsteps.app.ui.common.label

/**
 * Ce qu'on exporte : streak, niveaux, quelques souvenirs (CLAUDE.md §4).
 * isPremium pilote l'enrichissement du rendu (§7) : plus de souvenirs, photos
 * incluses — réservés au premium.
 */
data class ExportData(
    val globalProgress: GlobalProgressEntity?,
    val mediumProgress: List<MediumProgressEntity>,
    val recentSouvenirs: List<PortfolioEntryEntity>,
    val isPremium: Boolean = false
)

/**
 * Dessine le résumé de progression sur un Canvas natif. Bitmap et
 * PdfDocument.Page exposent tous les deux un Canvas standard : cette même
 * fonction sert donc aux formats image et PDF (voir ProgressExportGenerator),
 * pas de logique de mise en page dupliquée.
 *
 * Couleurs recopiées de ui/theme/Color.kt : un Canvas natif n'a pas accès au
 * MaterialTheme Compose. [context] sert uniquement à résoudre les chaînes
 * traduites (res/values{,-en}/strings.xml) — pas de UI ici.
 */
object ExportRenderer {
    const val WIDTH = 1080
    const val HEIGHT = 1350

    private const val MARGIN = 60f
    private const val THUMB_SIZE = 96
    private const val FREE_SOUVENIR_LIMIT = 5
    private const val PREMIUM_SOUVENIR_LIMIT = 10

    fun draw(canvas: Canvas, data: ExportData, context: Context) {
        canvas.drawColor(Color.parseColor("#FFFBFE"))

        val titlePaint = textPaint(color = "#1C1B1F", size = 56f, bold = true)
        val subtitlePaint = textPaint(color = "#49454F", size = 32f)
        val sectionPaint = textPaint(color = "#6650A4", size = 38f, bold = true)
        val bodyPaint = textPaint(color = "#1C1B1F", size = 30f)
        val premiumPaint = textPaint(color = "#B08300", size = 28f, bold = true)

        var y = 100f
        canvas.drawText("LittleBigSteps", MARGIN, y, titlePaint)
        if (data.isPremium) {
            canvas.drawText(context.getString(R.string.export_premium_badge), WIDTH - 300f, y, premiumPaint)
        }
        y += 50f
        canvas.drawText(context.getString(R.string.export_recap_title), MARGIN, y, subtitlePaint)
        y += 90f

        canvas.drawText(context.getString(R.string.export_streak_title), MARGIN, y, sectionPaint)
        y += 50f
        val global = data.globalProgress
        canvas.drawText(
            context.getString(R.string.export_streak_days, global?.currentStreak ?: 0),
            MARGIN,
            y,
            bodyPaint
        )
        y += 42f
        canvas.drawText(
            context.getString(R.string.export_streak_record, global?.longestStreak ?: 0),
            MARGIN,
            y,
            bodyPaint
        )
        y += 42f
        canvas.drawText(
            context.getString(R.string.export_total_completed, global?.totalChallengesCompleted ?: 0),
            MARGIN,
            y,
            bodyPaint
        )
        y += 70f

        canvas.drawText(context.getString(R.string.export_progress_by_medium), MARGIN, y, sectionPaint)
        y += 50f
        data.mediumProgress.forEach { progress ->
            val status = if (progress.isUnlocked) {
                context.getString(
                    R.string.export_medium_status_unlocked,
                    progress.level,
                    progress.xp,
                    progress.challengesCompletedCount
                )
            } else {
                context.getString(R.string.locked_premium_description)
            }
            canvas.drawText("${progress.mediumType.label(context)} — $status", MARGIN, y, bodyPaint)
            y += 42f
        }
        y += 30f

        if (data.recentSouvenirs.isNotEmpty()) {
            canvas.drawText(context.getString(R.string.export_recent_souvenirs), MARGIN, y, sectionPaint)
            y += 50f
            val limit = if (data.isPremium) PREMIUM_SOUVENIR_LIMIT else FREE_SOUVENIR_LIMIT
            data.recentSouvenirs.take(limit).forEach { entry ->
                val note = entry.completion.souvenirNote
                val title = entry.challengeTitle ?: context.getString(R.string.export_untitled_challenge)
                val line = if (note != null) "« $note » — $title" else title
                val photoPath = entry.completion.souvenirPhotoPath

                // Photo incluse dans l'export : réservé au premium (§7). En
                // gratuit, seul le texte du souvenir est repris, comme avant.
                val thumbnail = if (data.isPremium && photoPath != null) {
                    decodeSquareBitmap(photoPath, THUMB_SIZE)
                } else {
                    null
                }

                if (thumbnail != null) {
                    canvas.drawBitmap(thumbnail, MARGIN, y - THUMB_SIZE + 16f, null)
                    canvas.drawText(truncate(line, 46), MARGIN + THUMB_SIZE + 20f, y - THUMB_SIZE / 2f + 16f, bodyPaint)
                    thumbnail.recycle()
                    y += THUMB_SIZE + 16f
                } else {
                    canvas.drawText(truncate(line, 60), MARGIN, y, bodyPaint)
                    y += 42f
                }
            }
        }
    }

    private fun textPaint(color: String, size: Float, bold: Boolean = false): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.parseColor(color)
            textSize = size
            isFakeBoldText = bold
        }

    private fun truncate(text: String, maxChars: Int): String =
        if (text.length <= maxChars) text else text.take(maxChars - 1) + "…"

    /** Décodage sous-échantillonné + recadrage carré centré, pour une grille de
     *  miniatures régulière dans l'export. Différent de ui/common/LocalPhotoThumbnail
     *  (pas de recadrage carré là-bas) : besoins de mise en page différents. */
    private fun decodeSquareBitmap(path: String, size: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0) return null // fichier absent ou corrompu

        var inSampleSize = 1
        val smallestSide = minOf(bounds.outWidth, bounds.outHeight)
        while (smallestSide / inSampleSize >= size * 2) {
            inSampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        val decoded = BitmapFactory.decodeFile(path, options) ?: return null

        val cropSize = minOf(decoded.width, decoded.height)
        val cropX = (decoded.width - cropSize) / 2
        val cropY = (decoded.height - cropSize) / 2
        val cropped = Bitmap.createBitmap(decoded, cropX, cropY, cropSize, cropSize)
        val scaled = Bitmap.createScaledBitmap(cropped, size, size, true)
        if (decoded !== cropped) decoded.recycle()
        if (cropped !== scaled) cropped.recycle()
        return scaled
    }
}
