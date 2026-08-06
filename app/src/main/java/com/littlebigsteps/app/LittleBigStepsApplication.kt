package com.littlebigsteps.app

import android.app.Application
import com.littlebigsteps.app.data.local.AppDatabase

/**
 * Point d'accès manuel (service locator léger) à la base locale. Pas de framework
 * DI au stade squelette — à revisiter (Hilt ?) si la complexité le justifie.
 */
class LittleBigStepsApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
