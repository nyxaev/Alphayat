package com.exemplo.cameraxdemo.dominio

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

enum class TipoPonto {
    ORELHA_ESQUERDA,
    ORELHA_DIREITA,
    OMBRO_ESQUERDO,
    OMBRO_DIREITO,
    COTOVELO_ESQUERDO,
    COTOVELO_DIREITO,
    PULSO_ESQUERDO,
    PULSO_DIREITO
}

data class PontoCorpo(
    val x: Float,
    val y: Float,
    val confianca: Float
)

data class MetricasPose(
    val inclinacaoOmbrosGraus: Float,
    val deslocamentoCabecaRazao: Float,
    val bracosCruzados: Boolean
)

object CalculadoraPose {

    private const val CONFIANCA_MINIMA = 0.5f
    private const val LARGURA_OMBROS_MINIMA_PX = 20f
    private const val DISTANCIA_CRUZAMENTO_RAZAO = 0.65f

    fun calcular(pontos: Map<TipoPonto, PontoCorpo>): MetricasPose? {
        val ombroE = pontos[TipoPonto.OMBRO_ESQUERDO]
        val ombroD = pontos[TipoPonto.OMBRO_DIREITO]

        if (ombroE == null || ombroD == null) return null
        if (ombroE.confianca < CONFIANCA_MINIMA || ombroD.confianca < CONFIANCA_MINIMA) return null

        val dxOmbros = ombroD.x - ombroE.x
        val dyOmbros = ombroD.y - ombroE.y
        val larguraOmbros = hypot(dxOmbros, dyOmbros)

        if (larguraOmbros < LARGURA_OMBROS_MINIMA_PX) return null

        val inclinacaoBruta = Math.toDegrees(atan2(dyOmbros.toDouble(), dxOmbros.toDouble())).toFloat()
        val inclinacaoOmbros = normalizarAngulo(inclinacaoBruta)

        val orelhaE = pontos[TipoPonto.ORELHA_ESQUERDA]
        val orelhaD = pontos[TipoPonto.ORELHA_DIREITA]
        val deslocamentoCabeca = if (orelhaE != null && orelhaD != null &&
            orelhaE.confianca >= CONFIANCA_MINIMA && orelhaD.confianca >= CONFIANCA_MINIMA) {
            val centroOmbrosX = (ombroE.x + ombroD.x) / 2f
            val centroOrelhasX = (orelhaE.x + orelhaD.x) / 2f
            abs(centroOrelhasX - centroOmbrosX) / larguraOmbros
        } else {
            0f
        }

        val cotoveloE = pontos[TipoPonto.COTOVELO_ESQUERDO]
        val cotoveloD = pontos[TipoPonto.COTOVELO_DIREITO]
        val pulsoE = pontos[TipoPonto.PULSO_ESQUERDO]
        val pulsoD = pontos[TipoPonto.PULSO_DIREITO]

        val bracosCruzados = if (cotoveloE != null && cotoveloD != null && pulsoE != null && pulsoD != null &&
            cotoveloE.confianca >= CONFIANCA_MINIMA && cotoveloD.confianca >= CONFIANCA_MINIMA &&
            pulsoE.confianca >= CONFIANCA_MINIMA && pulsoD.confianca >= CONFIANCA_MINIMA) {
            val pulsosAbaixoOmbros = pulsoE.y >= ombroE.y - (larguraOmbros * 0.2f) &&
                                     pulsoD.y >= ombroD.y - (larguraOmbros * 0.2f)
            val distE = hypot(pulsoE.x - cotoveloD.x, pulsoE.y - cotoveloD.y)
            val distD = hypot(pulsoD.x - cotoveloE.x, pulsoD.y - cotoveloE.y)
            pulsosAbaixoOmbros && distE < larguraOmbros * DISTANCIA_CRUZAMENTO_RAZAO &&
                                  distD < larguraOmbros * DISTANCIA_CRUZAMENTO_RAZAO
        } else {
            false
        }

        return MetricasPose(
            inclinacaoOmbrosGraus = inclinacaoOmbros,
            deslocamentoCabecaRazao = deslocamentoCabeca,
            bracosCruzados = bracosCruzados
        )
    }

    private fun normalizarAngulo(graus: Float): Float {
        var resultado = graus
        while (resultado > 90f) resultado -= 180f
        while (resultado < -90f) resultado += 180f
        return resultado
    }
}
