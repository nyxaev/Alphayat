package com.exemplo.cameraxdemo.apresentacao

const val MENSAGEM_RECUSA = "Desculpe, não foi possível realizar esta análise para a solicitação enviada."
const val MENSAGEM_ERRO = "Não foi possível concluir a análise. Verifique se o servidor backend está em execução."
const val MENSAGEM_SEM_MICROFONE = "Permissão do microfone necessária para analisar a voz."

sealed interface EstadoAnalise {
    data object Ocioso : EstadoAnalise
    data class Carregando(val mensagem: String = "Analisando...") : EstadoAnalise
    data class Sucesso(
        val pose: String?,
        val roupa: String?,
        val voz: String?
    ) : EstadoAnalise
    data object Recusado : EstadoAnalise
    data class Erro(val mensagem: String) : EstadoAnalise
}
