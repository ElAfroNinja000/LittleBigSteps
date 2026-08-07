package com.littlebigsteps.app.export

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.littlebigsteps.app.data.local.entity.GlobalProgressEntity
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.data.local.entity.PortfolioEntryEntity
import com.littlebigsteps.app.domain.model.Badge
import com.littlebigsteps.app.ui.common.label

/**
 * Ce qu'on exporte : streak, niveaux, quelques souvenirs (CLAUDE.md §4).
 * isPremium/unlockedBadges pilotent l'enrichissement du rendu (§7) : plus de
 * souvenirs, photos incluses, badges affichés — réservés au premium.
 */
data class ExportData(
    val globalProgress: GlobalProgressEntity?,
    val mediumProgress: List<MediumProgressEntity>,
    val recentSouvenirs: List<PortfolioEntryEntity>,
    val isPremium: Boolean = false,
    val unlockedBadges: Set<Badge> = emptySet()
)

/**
 * Dessine le résumé de progression sur un Canvas natif. Bitmap et
 * PdfDocument.Page exposent tous les deux un Canvas standard : cette même
 * fonction sert donc aux formats image et PDF (voir ProgressExportGenerator),
 * pas de logique de mise en page dupliquée. [drawStory] est un format visuel
 * distinct, exclusif premium (CLAUDE.md §7).
 *
 * Couleurs recopiées de ui/theme/Color.kt : un Canvas natif n'a pas accès au
 * MaterialTheme Compose.
 */
object ExportRenderer {
    const val WIDTH = 1080
    const val HEIGHT = 1350
    const val STORY_WIDTH = 1080
    const val STORY_HEIGHT = 1920

    private const val MARGIN = 60f
    private const val THUMB_SIZE = 96
    private const val FREE_SOUVENIR_LIMIT = 5
    private const val PREMIUM_SOUVENIR_LIMIT = 10

    fun draw(canvas: Canvas, data: ExportData) {
        canvas.drawColor(Color.parseColor("#FFFBFE"))

        val titlePaint = textPaint(color = "#1C1B1F", size = 56f, bold = true)
        val subtitlePaint = textPaint(color = "#49454F", size = 32f)
        val sectionPaint = textPaint(color = "#6650A4", size = 38f, bold = true)
        val bodyPaint = textPaint(color = "#1C1B1F", size = 30f)
        val premiumPaint = textPaint(color = "#B08300", size = 28f, bold = true)

        var y = 100f
        canvas.drawText("LittleBigSteps", MARGIN, y, titlePaint)
        if (data.isPremium) {
            canvas.drawText("★ PREMIUM", WIDTH - 300f, y, premiumPaint)
        }
        y += 50f
        canvas.drawText("Récap de progression", MARGIN, y, subtitlePaint)
        y += 90f

        canvas.drawText("Streak", MARGIN, y, sectionPaint)
        y += 50f
        val global = data.globalProgress
        canvas.drawText("${global?.currentStreak ?: 0} jour(s) de suite", MARGIN, y, bodyPaint)
        y += 42f
        canvas.drawText("Record : ${global?.longestStreak ?: 0} jour(s)", MARGIN, y, bodyPaint)
        y += 42f
        canvas.drawText(
            "${global?.totalChallengesCompleted ?: 0} défis complétés au total",
            MARGIN,
            y,
            bodyPaint
        )
        y += 70f

        canvas.drawText("Progression par médium", MARGIN, y, sectionPaint)
        y += 50f
        data.mediumProgress.forEach { progress ->
            val status = if (progress.isUnlocked) {
                "Niveau ${progress.level} · ${progress.xp} XP · ${progress.challengesCompletedCount} défi(s)"
            } else {
                "Verrouillé (premium)"
            }
            canvas.drawText("${progress.mediumType.label()} — $status", MARGIN, y, bodyPaint)
            y += 42f
        }
        y += 30f

        // Badges : section exclusive premium (CLAUDE.md §7), absente pour un
        // export gratuit même si (en théorie) aucun badge n'existerait de toute
        // façon puisqu'ils ne sont évalués que pour les utilisateurs premium.
        if (data.isPremium && data.unlockedBadges.isNotEmpty()) {
            canvas.drawText("Badges débloqués", MARGIN, y, sectionPaint)
            y += 50f
            data.unlockedBadges.forEach { badge ->
                canvas.drawText("🏅 ${badge.label()}", MARGIN, y, bodyPaint)
                y += 42f
            }
            y += 30f
        }

        if (data.recentSouvenirs.isNotEmpty()) {
            canvas.drawText("Derniers souvenirs", MARGIN, y, sectionPaint)
            y += 50f
            val limit = if (data.isPremium) PREMIUM_SOUVENIR_LIMIT else FREE_SOUVENIR_LIMIT
            data.recentSouvenirs.take(limit).forEach { entry ->
                val note = entry.completion.souvenirNote
                val title = entry.challengeTitle ?: "Défi"
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

    /**
     * Format "story" (ratio réseaux sociaux, 9:16) : mise en page distincte,
     * plus visuelle, exclusive premium (§7) — pas un simple redimensionnement
     * du résumé classique.
     */
    fun drawStory(canvas: Canvas, data: ExportData) {
        canvas.drawColor(Color.parseColor("#6650A4"))

        val titlePaint = textPaint(color = "#FFFFFF", size = 48f, bold = true)
        val hugePaint = textPaint(color = "#FFFFFF", size = 220f, bold = true)
        val labelPaint = textPaint(color = "#EADDFF", size = 36f)
        val bodyPaint = textPaint(color = "#FFFFFF", size = 34f)

        var y = 160f
        canvas.drawText("LittleBigSteps", MARGIN, y, titlePaint)
        y += 260f

        val global = data.globalProgress
        val streak = global?.currentStreak ?: 0
        canvas.drawText("$streak", MARGIN, y, hugePaint)
        y += 70f
        canvas.drawText(if (streak > 1) "jours de suite" else "jour de suite", MARGIN, y, labelPaint)
        y += 140f

        canvas.drawText("${global?.totalChallengesCompleted ?: 0} défis complétés au total", MARGIN, y, bodyPaint)
        y += 90f

        data.mediumProgress.filter { it.isUnlocked }.forEach { progress ->
            canvas.drawText("${progress.mediumType.label()} · niveau ${progress.level}", MARGIN, y, bodyPaint)
            y += 56f
        }

        if (data.unlockedBadges.isNotEmpty()) {
            y += 40f
            canvas.drawText("🏅 ${data.unlockedBadges.size} badge(s) débloqué(s)", MARGIN, y, bodyPaint)
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
