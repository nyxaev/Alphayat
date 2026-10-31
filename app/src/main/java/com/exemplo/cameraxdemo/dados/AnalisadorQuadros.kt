package com.exemplo.cameraxdemo.dados

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.exemplo.cameraxdemo.dominio.ProcessadorQuadro
import com.exemplo.cameraxdemo.dominio.ResultadoQuadro

class AnalisadorQuadros(
    private val processador: ProcessadorQuadro,
    private val aoProcessar: (ResultadoQuadro, Float) -> Unit
) : ImageAnalysis.Analyzer {

    private var ultimoTimestampNs = 0L
    private var fpsSuavizado = 0f

    override fun analyze(image: ImageProxy) {
        try {
            val agoraNs = image.imageInfo.timestamp
            if (ultimoTimestampNs != 0L) {
                val deltaNs = agoraNs - ultimoTimestampNs
                if (deltaNs > 0) {
                    val fpsInstantaneo = 1_000_000_000f / deltaNs
                    fpsSuavizado = if (fpsSuavizado == 0f) {
                        fpsInstantaneo
                    } else {
                        fpsSuavizado * 0.9f + fpsInstantaneo * 0.1f
                    }
                }
            }
            ultimoTimestampNs = agoraNs
            aoProcessar(processador.processar(image), fpsSuavizado)
        } finally {
            image.close()
        }
    }
}
