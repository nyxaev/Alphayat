package com.exemplo.cameraxdemo

import com.exemplo.cameraxdemo.dominio.CalculadoraVoz
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

class CalculadoraVozTest {

    @Test
    fun `calcular com amostras curtas deve retornar null`() {
        val poucasAmostras = ShortArray(1000)
        val resultado = CalculadoraVoz.calcular(poucasAmostras)
        assertNull(resultado)
    }

    @Test
    fun `calcular com amostras em silencio deve retornar volume baixo e zero pausas`() {
        val silencio = ShortArray(16000 * 2) // 2 segundos de silencio
        val resultado = CalculadoraVoz.calcular(silencio)
        assertNotNull(resultado)
        assertTrue(resultado!!.volumeDb < -90f)
        assertEquals(0, resultado.numeroPausas)
        assertEquals(0f, resultado.velocidadeSilabasPorSegundo, 0.01f)
    }

    @Test
    fun `calcular com tom de onda senoidal sintetica deve calcular volume e duracao corretamente`() {
        val taxa = 16000
        val duracaoSeg = 2
        val amostras = ShortArray(taxa * duracaoSeg)
        val freq = 220.0 // 220 Hz (A3)
        for (i in amostras.indices) {
            val t = i.toDouble() / taxa
            amostras[i] = (sin(2.0 * Math.PI * freq * t) * 16000).toInt().toShort()
        }

        val resultado = CalculadoraVoz.calcular(amostras)
        assertNotNull(resultado)
        assertEquals(2.0f, resultado!!.duracaoSegundos, 0.1f)
        assertTrue(resultado.volumeDb > -30f)
    }
}
