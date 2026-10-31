package com.exemplo.cameraxdemo

import com.exemplo.cameraxdemo.dados.ParserRespostaAnalise
import com.exemplo.cameraxdemo.dominio.ResultadoAnalise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParserRespostaAnaliseTest {

    private val parser = ParserRespostaAnalise()

    @Test
    fun `interpretar json de sucesso valido deve retornar Sucesso`() {
        val json = """
            {
              "dentro_do_escopo": true,
              "pose": "Ombros bem alinhados.",
              "roupa": "Boa combinação.",
              "voz": "Ritmo constante.",
              "modo_demonstracao": false
            }
        """.trimIndent()

        val resultado = parser.interpretar(json)
        assertTrue(resultado is ResultadoAnalise.Sucesso)
        val sucesso = resultado as ResultadoAnalise.Sucesso
        assertEquals("Ombros bem alinhados.", sucesso.pose)
        assertEquals("Boa combinação.", sucesso.roupa)
        assertEquals("Ritmo constante.", sucesso.voz)
        assertFalse(sucesso.modoDemonstracao)
    }

    @Test
    fun `interpretar json de modo demonstracao deve marcar modoDemonstracao true`() {
        val json = """
            {
              "dentro_do_escopo": true,
              "pose": "Postura alinhada.",
              "modo_demonstracao": true
            }
        """.trimIndent()

        val resultado = parser.interpretar(json)
        assertTrue(resultado is ResultadoAnalise.Sucesso)
        val sucesso = resultado as ResultadoAnalise.Sucesso
        assertTrue(sucesso.modoDemonstracao)
    }

    @Test
    fun `interpretar json envolvido em bloco markdown deve ser tratado corretamente`() {
        val json = """
            ```json
            {
              "dentro_do_escopo": true,
              "pose": "Postura ok."
            }
            ```
        """.trimIndent()

        val resultado = parser.interpretar(json)
        assertTrue(resultado is ResultadoAnalise.Sucesso)
    }

    @Test
    fun `interpretar json fora do escopo deve retornar Recusado`() {
        val json = """
            {
              "dentro_do_escopo": false,
              "pose": null,
              "roupa": null,
              "voz": null
            }
        """.trimIndent()

        val resultado = parser.interpretar(json)
        assertTrue(resultado is ResultadoAnalise.Recusado)
    }

    @Test
    fun `interpretar json malformatado deve retornar Recusado`() {
        val resultado = parser.interpretar("texto aleatorio nao json")
        assertTrue(resultado is ResultadoAnalise.Recusado)
    }
}
