package com.exemplo.cameraxdemo.dados

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import kotlin.math.min

object CodificadorImagem {

    private const val LADO_MAXIMO = 512
    private const val QUALIDADE_JPEG = 80

    fun paraBase64(imagem: ImageProxy): String {
        var originalBitmap: Bitmap? = null
        var redimensionadaBitmap: Bitmap? = null
        try {
            originalBitmap = imagem.toBitmap()
            val escala = min(1f, LADO_MAXIMO / maxOf(originalBitmap.width, originalBitmap.height).toFloat())
            val matriz = Matrix().apply {
                postRotate(imagem.imageInfo.rotationDegrees.toFloat())
                postScale(escala, escala)
            }
            redimensionadaBitmap = Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matriz, true
            )
            val saida = ByteArrayOutputStream()
            redimensionadaBitmap.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, saida)
            return Base64.encodeToString(saida.toByteArray(), Base64.NO_WRAP)
        } finally {
            if (redimensionadaBitmap != null && redimensionadaBitmap != originalBitmap && !redimensionadaBitmap.isRecycled) {
                redimensionadaBitmap.recycle()
            }
            if (originalBitmap != null && !originalBitmap.isRecycled) {
                originalBitmap.recycle()
            }
            imagem.close()
        }
    }
}
