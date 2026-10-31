package com.exemplo.cameraxdemo.dominio

sealed interface ResultadoAnalise {
    data class Sucesso(
        val pose: String?,
        val roupa: String?,
        val voz: String?,
        val modoDemonstracao: Boolean = false
    ) : ResultadoAnalise

    data object Recusado : ResultadoAnalise
}
