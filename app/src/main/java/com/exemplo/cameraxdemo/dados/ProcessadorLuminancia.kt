package com.exemplo.cameraxdemo.dados

import androidx.camera.core.ImageProxy
import com.exemplo.cameraxdemo.dominio.ProcessadorQuadro
import com.exemplo.cameraxdemo.dominio.ResultadoQuadro

class ProcessadorLuminancia(
    private val passoAmostragem: Int = 16
) : ProcessadorQuadro {

    override fun processar(imagem: ImageProxy): ResultadoQuadro {
        val buffer = imagem.planes[0].buffer
        val limite = buffer.limit()
        var soma = 0L
        var amostras = 0
        var indice = 0
        while (indice < limite) {
            soma += buffer.get(indice).toInt() and 0xFF
            amostras++
            indice += passoAmostragem
        }
        val media = if (amostras > 0) (soma / amostras).toInt() else 0
        return ResultadoQuadro(
            largura = imagem.width,
            altura = imagem.height,
            rotacao = imagem.imageInfo.rotationDegrees,
            luminanciaMedia = media
        )
    }
}
