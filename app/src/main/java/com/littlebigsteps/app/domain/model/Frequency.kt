package com.littlebigsteps.app.domain.model

import kotlin.math.roundToLong

/**
 * Fréquence de rappel choisie à l'onboarding : nombre de fois par semaine
 * (1 à 7, choisi via une roue de sélection numérique). Pilote à la fois les
 * notifications locales (WorkManager) et le délai avant que de nouvelles
 * activités soient reproposées (voir ChallengeRepositoryImpl).
 *
 * Classe normale plutôt que `value class` : Room/KSP ne sait pas générer de
 * getter pour un champ d'entité typé en value class sans l'option
 * expérimentale `room.generateKotlin`, même avec un TypeConverter déclaré.
 */
data class Frequency(val timesPerWeek: Int) {
    init {
        require(timesPerWeek in 1..7) {
            "timesPerWeek doit être entre 1 et 7 (reçu: $timesPerWeek)"
        }
    }

    /** Nombre de jours entre deux occasions (rappel ou nouvelle activité
     *  proposée), arrondi au jour entier le plus proche (minimum 1 jour). Pour
     *  les valeurs qui ne divisent pas 7 rond (ex. 4 fois/semaine -> tous les
     *  2 jours), un léger écart par rapport au nombre demandé est possible —
     *  acceptable pour un rythme motivant plutôt qu'un calcul au jour près. */
    val repeatIntervalDays: Int
        get() = (7.0 / timesPerWeek).roundToLong().coerceAtLeast(1).toInt()
}
