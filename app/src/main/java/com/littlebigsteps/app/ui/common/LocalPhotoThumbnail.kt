package com.littlebigsteps.app.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Miniature d'une photo de souvenir stockée en local. Décodage sous-échantillonné
 * (évite de charger l'image plein format pour un simple aperçu) et hors du thread
 * principal — pas de lib de chargement d'image (Coil/Glide) pour un aperçu aussi
 * simple, volontairement minimal.
 */
@Composable
fun LocalPhotoThumbnail(path: String, modifier: Modifier = Modifier.size(96.dp)) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = path) {
        value = withContext(Dispatchers.IO) { decodeSampledBitmap(path, reqSize = 200) }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

private fun decodeSampledBitmap(path: String, reqSize: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0) return null // fichier absent ou corrompu

    var inSampleSize = 1
    if (bounds.outHeight > reqSize || bounds.outWidth > reqSize) {
        val halfHeight = bounds.outHeight / 2
        val halfWidth = bounds.outWidth / 2
        while (halfHeight / inSampleSize >= reqSize && halfWidth / inSampleSize >= reqSize) {
            inSampleSize *= 2
        }
    }

    val options = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
    return BitmapFactory.decodeFile(path, options)
}
