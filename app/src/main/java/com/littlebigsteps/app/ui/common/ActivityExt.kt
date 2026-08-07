package com.littlebigsteps.app.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** LocalContext.current n'est pas toujours directement une Activity (peut être
 *  enveloppé) — nécessaire pour lancer le flux d'achat Play Billing. */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
