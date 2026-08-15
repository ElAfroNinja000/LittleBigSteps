package com.littlebigsteps.app.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Calendrier de renouvellement des nouvelles activités (CLAUDE.md §3-4) : tant
 * qu'une activité d'un médium est "en cours", aucune nouvelle proposition
 * n'apparaît pour ce médium — sauf si le délai issu de la fréquence choisie à
 * l'onboarding ("combien de fois par semaine ?") est écoulé depuis le
 * démarrage de la dernière activité de ce médium. C'est la seule façon
 * d'accumuler plusieurs activités en cours à la fois : le renouvellement
 * périodique, jamais en choisissant plusieurs propositions d'un même lot.
 *
 * Le calendrier n'étale pas juste "7 / fréquence" jours de façon uniforme :
 * pour 2, 3 et 4 fois par semaine, les jours de renouvellement sont espacés
 * (ex. 3x/semaine -> jours 1, 4, 7) plutôt que consécutifs, pour répartir la
 * pratique sur la semaine plutôt que la concentrer en début de cycle.
 */
object RenewalSchedule {

    /** Jours du cycle de 7 jours (1-indexés, jour 1 = jour de démarrage de la
     *  dernière activité en cours) où un nouveau lot doit être proposé. */
    private val RENEWAL_DAYS: Map<Int, Set<Int>> = mapOf(
        1 to setOf(1),
        2 to setOf(1, 4),
        3 to setOf(1, 4, 7),
        4 to setOf(1, 3, 5, 7),
        5 to setOf(1, 2, 3, 4, 5),
        6 to setOf(1, 2, 3, 4, 5, 6),
        7 to setOf(1, 2, 3, 4, 5, 6, 7)
    )

    /** true si un nouveau lot doit être proposé malgré une activité encore en
     *  cours : le jour courant (depuis [startedAt], cycle de 7 jours) est un
     *  jour de renouvellement, et au moins un jour plein s'est écoulé depuis
     *  le démarrage (le jour 1 est le jour de démarrage lui-même, pas un
     *  renouvellement à proprement parler). */
    fun isRenewalDue(
        startedAt: Instant,
        timesPerWeek: Int,
        now: Instant = Clock.System.now()
    ): Boolean {
        val daysSinceStart = (now - startedAt).inWholeDays.toInt()
        if (daysSinceStart <= 0) return false
        val renewalDays = RENEWAL_DAYS[timesPerWeek.coerceIn(1, 7)] ?: return false
        // Jour 1 (démarrage) = daysSinceStart 0 ; le cycle de 7 jours reboucle
        // ensuite sur "jour 1" tous les 7 jours pleins écoulés.
        val dayInCycle = (daysSinceStart % 7) + 1
        return dayInCycle in renewalDays
    }
}
