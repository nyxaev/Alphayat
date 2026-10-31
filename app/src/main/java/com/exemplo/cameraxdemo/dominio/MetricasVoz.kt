package com.exemplo.cameraxdemo.dominio

data class MetricasVoz(
    val volumeDb: Float,
    val variacaoTomSemitons: Float,
    val velocidadeSilabasPorSegundo: Float,
    val numeroPausas: Int,
    val duracaoSegundos: Float
)
