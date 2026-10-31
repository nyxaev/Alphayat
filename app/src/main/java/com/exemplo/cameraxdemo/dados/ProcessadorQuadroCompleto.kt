package com.exemplo.cameraxdemo.dados

import androidx.camera.core.ImageProxy
import com.exemplo.cameraxdemo.dominio.ProcessadorQuadro
import com.exemplo.cameraxdemo.dominio.ResultadoQuadro

class ProcessadorQuadroCompleto(
    private val luminancia: ProcessadorLuminancia,
    private val pose: ProcessadorPose
) : ProcessadorQuadro {

    override fun processar(imagem: ImageProxy): ResultadoQuadro =
        luminancia.processar(imagem).copy(metricasPose = pose.processar(imagem))
}
