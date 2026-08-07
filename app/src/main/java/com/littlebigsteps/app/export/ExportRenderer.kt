package com.littlebigsteps.app.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.littlebigsteps.app.data.local.entity.GlobalProgressEntity
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.data.local.entity.PortfolioEntryEntity
import com.littlebigsteps.app.ui.common.label

/** Ce qu'on exporte : streak, niveaux, quelques souvenirs (CLAUDE.md §4). */
data class ExportData(
    val globalProgress: GlobalProgressEntity?,
    val mediumProgress: List<MediumProgressEntity>,
    val recentSouvenirs: List<PortfolioEntryEntity>
)

/**
 * Dessine le résumé de progression sur un Canvas natif. Bitmap et
 * PdfDocument.Page exposent tous les deux un Canvas standard : cette même
 * fonction sert donc aux deux formats d'export (voir ProgressExportGenerator),
 * pas de logique de mise en page dupliquée.
 *
 * Couleurs recopiées de ui/theme/Color.kt : un Canvas natif n'a pas accès au
 * MaterialTheme Compose.
 */
object ExportRenderer {
    const val WIDTH = 1080
    const val HEIGHT = 1350

    private const val MARGIN = 60f

    fun draw(canvas: Canvas, data: ExportData) {
        canvas.drawColor(Color.parseColor("#FFFBFE"))

        val titlePaint = textPaint(color = "#1C1B1F", size = 56f, bold = true)
        val subtitlePaint = textPaint(color = "#49454F", size = 32f)
        val sectionPaint = textPaint(color = "#6650A4", size = 38f, bold = true)
        val bodyPaint = textPaint(color = "#1C1B1F", size = 30f)

        var y = 100f
        canvas.drawText("LittleBigSteps", MARGIN, y, titlePaint)
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

        if (data.recentSouvenirs.isNotEmpty()) {
            canvas.drawText("Derniers souvenirs", MARGIN, y, sectionPaint)
            y += 50f
            data.recentSouvenirs.take(5).forEach { entry ->
                val note = entry.completion.souvenirNote
                val title = entry.challengeTitle ?: "Défi"
                val line = if (note != null) "« $note » — $title" else title
                canvas.drawText(truncate(line, 60), MARGIN, y, bodyPaint)
                y += 42f
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
}
