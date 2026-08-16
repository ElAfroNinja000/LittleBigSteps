package com.littlebigsteps.app.ui.common

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Instant.toLocalDate(): LocalDate = toLocalDateTime(TimeZone.currentSystemDefault()).date

private val MONTHS_FR = listOf(
    "janvier", "février", "mars", "avril", "mai", "juin",
    "juillet", "août", "septembre", "octobre", "novembre", "décembre"
)

private val MONTHS_EN = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

/** Langue choisie dans l'app, avec repli sur la langue système si rien n'a été
 *  choisi explicitement. Même correctif que NetworkConfig.contentLocale() :
 *  sur Android 12 et moins, Locale.getDefault() ne reflète pas un changement de
 *  langue fait dans les Paramètres pendant la session (voir NetworkConfig.kt
 *  pour le détail). Sans ce contournement, les dates restaient dans l'ancienne
 *  langue alors que le reste de l'interface avait déjà changé. */
private fun preferredLanguage(): String {
    val appLocales = AppCompatDelegate.getApplicationLocales()
    return if (!appLocales.isEmpty) appLocales[0]?.language ?: Locale.getDefault().language
    else Locale.getDefault().language
}

/** Format simple et lisible selon la langue de l'app — pas de dépendance à une
 *  lib de formatting. Ex fr : "6 août 2026" ; en : "August 6, 2026" (convention
 *  jour-mois vs mois-jour propre à chaque langue, pas juste une traduction des
 *  noms de mois). */
fun LocalDate.toDisplayString(): String =
    if (preferredLanguage() == "en") {
        "${MONTHS_EN[monthNumber - 1]} $dayOfMonth, $year"
    } else {
        "$dayOfMonth ${MONTHS_FR[monthNumber - 1]} $year"
    }
