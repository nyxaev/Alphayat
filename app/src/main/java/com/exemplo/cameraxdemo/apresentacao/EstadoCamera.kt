package com.exemplo.cameraxdemo.apresentacao

import com.exemplo.cameraxdemo.dominio.MetricasPose

data class EstadoCamera(
    val largura: Int = 0,
    val altura: Int = 0,
    val rotacao: Int = 0,
    val luminanciaMedia: Int = 0,
    val fps: Float = 0f,
    val recebendoQuadros: Boolean = false,
    val metricasPose: MetricasPose? = null
)
