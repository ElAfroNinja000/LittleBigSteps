package com.littlebigsteps.app.ui.common

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

/** Format simple et lisible selon la langue système — pas de dépendance à une
 *  lib de formatting. Ex fr : "6 août 2026" ; en : "August 6, 2026" (convention
 *  jour-mois vs mois-jour propre à chaque langue, pas juste une traduction des
 *  noms de mois). */
fun LocalDate.toDisplayString(): String =
    if (Locale.getDefault().language == "en") {
        "${MONTHS_EN[monthNumber - 1]} $dayOfMonth, $year"
    } else {
        "$dayOfMonth ${MONTHS_FR[monthNumber - 1]} $year"
    }
