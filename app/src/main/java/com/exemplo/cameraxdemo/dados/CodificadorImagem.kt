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
        try {
            val original = imagem.toBitmap()
            val escala = min(1f, LADO_MAXIMO / maxOf(original.width, original.height).toFloat())
            val matriz = Matrix().apply {
                postRotate(imagem.imageInfo.rotationDegrees.toFloat())
                postScale(escala, escala)
            }
            val redimensionada = Bitmap.createBitmap(
                original, 0, 0, original.width, original.height, matriz, true
            )
            val saida = ByteArrayOutputStream()
            redimensionada.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, saida)
            return Base64.encodeToString(saida.toByteArray(), Base64.NO_WRAP)
        } finally {
            imagem.close()
        }
    }
}
