package com.exemplo.cameraxdemo.dominio

sealed interface ResultadoAnalise {
    data class Sucesso(
        val pose: String?,
        val roupa: String?,
        val voz: String?
    ) : ResultadoAnalise

    data object Recusado : ResultadoAnalise
}
