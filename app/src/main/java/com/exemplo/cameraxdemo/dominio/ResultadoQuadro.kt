package com.exemplo.cameraxdemo.dominio

data class ResultadoQuadro(
    val largura: Int,
    val altura: Int,
    val rotacao: Int,
    val luminanciaMedia: Int,
    val metricasPose: MetricasPose? = null
)
