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
    private const val LARGURA_OMBROS_MINIMA = 1f
    private const val DISTANCIA_CRUZAMENTO = 0.6f

    fun calcular(pontos: Map<TipoPonto, PontoCorpo>): MetricasPose? {
        if (TipoPonto.entries.any { (pontos[it]?.confianca ?: 0f) < CONFIANCA_MINIMA }) return null

        val orelhaE = pontos.getValue(TipoPonto.ORELHA_ESQUERDA)
        val orelhaD = pontos.getValue(TipoPonto.ORELHA_DIREITA)
        val ombroE = pontos.getValue(TipoPonto.OMBRO_ESQUERDO)
        val ombroD = pontos.getValue(TipoPonto.OMBRO_DIREITO)
        val cotoveloE = pontos.getValue(TipoPonto.COTOVELO_ESQUERDO)
        val cotoveloD = pontos.getValue(TipoPonto.COTOVELO_DIREITO)
        val pulsoE = pontos.getValue(TipoPonto.PULSO_ESQUERDO)
        val pulsoD = pontos.getValue(TipoPonto.PULSO_DIREITO)

        val larguraOmbros = hypot(ombroD.x - ombroE.x, ombroD.y - ombroE.y)
        if (larguraOmbros < LARGURA_OMBROS_MINIMA) return null

        val inclinacao = normalizarAngulo(
            Math.toDegrees(atan2((ombroD.y - ombroE.y).toDouble(), (ombroD.x - ombroE.x).toDouble())).toFloat()
        )

        val centroOmbrosX = (ombroE.x + ombroD.x) / 2f
        val centroOrelhasX = (orelhaE.x + orelhaD.x) / 2f
        val deslocamento = abs(centroOrelhasX - centroOmbrosX) / larguraOmbros

        val pulsosAbaixoDosOmbros = pulsoE.y >= ombroE.y && pulsoD.y >= ombroD.y
        val pulsoEsqueVaiAoCotoveloDireito = hypot(pulsoE.x - cotoveloD.x, pulsoE.y - cotoveloD.y) < larguraOmbros * DISTANCIA_CRUZAMENTO
        val pulsoDirVaiAoCotoveloEsquerdo = hypot(pulsoD.x - cotoveloE.x, pulsoD.y - cotoveloE.y) < larguraOmbros * DISTANCIA_CRUZAMENTO

        return MetricasPose(
            inclinacaoOmbrosGraus = inclinacao,
            deslocamentoCabecaRazao = deslocamento,
            bracosCruzados = pulsosAbaixoDosOmbros && pulsoEsqueVaiAoCotoveloDireito && pulsoDirVaiAoCotoveloEsquerdo
        )
    }

    private fun normalizarAngulo(graus: Float): Float {
        var resultado = graus
        if (resultado > 90f) resultado -= 180f
        if (resultado < -90f) resultado += 180f
        return resultado
    }
}
