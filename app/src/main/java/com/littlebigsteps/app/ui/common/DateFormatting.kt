package com.littlebigsteps.app.ui.common

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Instant.toLocalDate(): LocalDate = toLocalDateTime(TimeZone.currentSystemDefault()).date

private val MONTHS_FR = listOf(
    "janvier", "février", "mars", "avril", "mai", "juin",
    "juillet", "août", "septembre", "octobre", "novembre", "décembre"
)

/** Format simple et lisible, ex: "6 août 2026" — pas de dépendance à une lib de formatting. */
fun LocalDate.toDisplayString(): String = "$dayOfMonth ${MONTHS_FR[monthNumber - 1]} $year"
