package com.exemplo.cameraxdemo.dados

import com.exemplo.cameraxdemo.dominio.ResultadoAnalise
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

class ParserRespostaAnalise {

    fun interpretar(texto: String): ResultadoAnalise {
        return try {
            val limpo = texto.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val raiz = Json.parseToJsonElement(limpo)
            if (raiz !is JsonObject) return ResultadoAnalise.Recusado

            val dentroDoEscopo = raiz.get(CHAVE_ESCOPO)
            if (dentroDoEscopo !is JsonPrimitive || dentroDoEscopo.isString) return ResultadoAnalise.Recusado
            val escopo = dentroDoEscopo.booleanOrNull ?: return ResultadoAnalise.Recusado
            if (!escopo) return ResultadoAnalise.Recusado

            val isDemo = (raiz.get("modo_demonstracao") as? JsonPrimitive)?.booleanOrNull ?: false

            ResultadoAnalise.Sucesso(
                pose = campoTexto(raiz, "pose"),
                roupa = campoTexto(raiz, "roupa"),
                voz = campoTexto(raiz, "voz"),
                modoDemonstracao = isDemo
            )
        } catch (_: Exception) {
            ResultadoAnalise.Recusado
        }
    }

    private fun campoTexto(objeto: JsonObject, chave: String): String? {
        val elemento = objeto.get(chave) ?: return null
        if (elemento is JsonNull) return null
        if (elemento !is JsonPrimitive || !elemento.isString) return null
        if (elemento.content.length > LIMITE_CARACTERES) return null
        return elemento.content
    }

    private companion object {
        const val CHAVE_ESCOPO = "dentro_do_escopo"
        const val LIMITE_CARACTERES = 1500
    }
}
