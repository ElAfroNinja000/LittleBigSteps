package com.littlebigsteps.app.domain

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * Langue choisie dans l'app : le choix explicite fait dans les Paramètres
 * (persisté par AppCompat, quel que soit l'OS) prime sur la langue système.
 *
 * Sur Android 12 et moins, AppCompatDelegate.setApplicationLocales() ne met à
 * jour que l'écran affiché au moment du changement, pas Locale.getDefault() :
 * lire Locale.getDefault() ailleurs dans l'app fait conclure à tort que la
 * langue n'a pas changé (voir NetworkConfig.contentLocale et
 * DateFormatting.toDisplayString, tous deux touchés avant ce correctif). Lire
 * directement AppCompatDelegate.getApplicationLocales() contourne ce défaut,
 * y compris hors d'un contexte d'Activity (coroutine, repository).
 */
fun preferredLanguage(): String {
    val appLocales = AppCompatDelegate.getApplicationLocales()
    return if (!appLocales.isEmpty) appLocales[0]?.language ?: Locale.getDefault().language
    else Locale.getDefault().language
}
