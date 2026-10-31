package com.exemplo.cameraxdemo.dominio

import androidx.camera.core.ImageProxy

fun interface ProcessadorQuadro {
    fun processar(imagem: ImageProxy): ResultadoQuadro
}
