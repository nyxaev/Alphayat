package com.exemplo.cameraxdemo.dados

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PedidoAnalise(
    val modo: String,
    @SerialName("imagem_jpeg_base64") val imagemJpegBase64: String?,
    @SerialName("metricas_pose") val metricasPose: MetricasPoseDto?,
    @SerialName("metricas_voz") val metricasVoz: MetricasVozDto?
)

@Serializable
data class MetricasPoseDto(
    @SerialName("inclinacao_ombros_graus") val inclinacaoOmbrosGraus: Float,
    @SerialName("deslocamento_cabeca_razao") val deslocamentoCabecaRazao: Float,
    @SerialName("bracos_cruzados") val bracosCruzados: Boolean
)

@Serializable
data class MetricasVozDto(
    @SerialName("volume_db") val volumeDb: Float,
    @SerialName("variacao_tom_semitons") val variacaoTomSemitons: Float,
    @SerialName("velocidade_silabas_por_segundo") val velocidadeSilabasPorSegundo: Float,
    @SerialName("numero_pausas") val numeroPausas: Int,
    @SerialName("duracao_segundos") val duracaoSegundos: Float
)
