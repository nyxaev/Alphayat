package com.exemplo.cameraxdemo

import com.exemplo.cameraxdemo.dominio.CalculadoraPose
import com.exemplo.cameraxdemo.dominio.PontoCorpo
import com.exemplo.cameraxdemo.dominio.TipoPonto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculadoraPoseTest {

    @Test
    fun `calcular com ombros ausentes deve retornar null`() {
        val pontos = mapOf(
            TipoPonto.ORELHA_ESQUERDA to PontoCorpo(100f, 100f, 0.9f)
        )
        val resultado = CalculadoraPose.calcular(pontos)
        assertNull(resultado)
    }

    @Test
    fun `calcular com ombros de baixa confianca deve retornar null`() {
        val pontos = mapOf(
            TipoPonto.OMBRO_ESQUERDO to PontoCorpo(100f, 200f, 0.2f),
            TipoPonto.OMBRO_DIREITO to PontoCorpo(300f, 200f, 0.9f)
        )
        val resultado = CalculadoraPose.calcular(pontos)
        assertNull(resultado)
    }

    @Test
    fun `calcular com ombros nivelados deve retornar inclinacao proxima de zero`() {
        val pontos = mapOf(
            TipoPonto.OMBRO_ESQUERDO to PontoCorpo(100f, 200f, 0.9f),
            TipoPonto.OMBRO_DIREITO to PontoCorpo(300f, 200f, 0.9f)
        )
        val resultado = CalculadoraPose.calcular(pontos)
        assertNotNull(resultado)
        assertEquals(0f, resultado!!.inclinacaoOmbrosGraus, 0.1f)
        assertFalse(resultado.bracosCruzados)
    }

    @Test
    fun `calcular sem orelhas deve funcionar sem falhar deslocamento de cabeca`() {
        val pontos = mapOf(
            TipoPonto.OMBRO_ESQUERDO to PontoCorpo(100f, 200f, 0.9f),
            TipoPonto.OMBRO_DIREITO to PontoCorpo(300f, 200f, 0.9f)
        )
        val resultado = CalculadoraPose.calcular(pontos)
        assertNotNull(resultado)
        assertEquals(0f, resultado!!.deslocamentoCabecaRazao, 0.01f)
    }

    @Test
    fun `calcular com bracos cruzados validos deve detectar bracos cruzados`() {
        val ombroE = PontoCorpo(100f, 200f, 0.9f)
        val ombroD = PontoCorpo(300f, 200f, 0.9f)
        val cotoveloE = PontoCorpo(120f, 300f, 0.9f)
        val cotoveloD = PontoCorpo(280f, 300f, 0.9f)
        val pulsoE = PontoCorpo(270f, 310f, 0.9f)
        val pulsoD = PontoCorpo(130f, 310f, 0.9f)

        val pontos = mapOf(
            TipoPonto.OMBRO_ESQUERDO to ombroE,
            TipoPonto.OMBRO_DIREITO to ombroD,
            TipoPonto.COTOVELO_ESQUERDO to cotoveloE,
            TipoPonto.COTOVELO_DIREITO to cotoveloD,
            TipoPonto.PULSO_ESQUERDO to pulsoE,
            TipoPonto.PULSO_DIREITO to pulsoD
        )

        val resultado = CalculadoraPose.calcular(pontos)
        assertNotNull(resultado)
        assertTrue(resultado!!.bracosCruzados)
    }
}
