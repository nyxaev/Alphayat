package com.exemplo.cameraxdemo.dados

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.exemplo.cameraxdemo.dominio.MetricasVoz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.sqrt

class CapturadorVoz {

    @SuppressLint("MissingPermission")
    suspend fun capturar(duracaoMs: Long = DURACAO_PADRAO_MS): MetricasVoz? = withContext(Dispatchers.IO) {
        val tamanhoMinimo = AudioRecord.getMinBufferSize(
            TAXA,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (tamanhoMinimo <= 0) return@withContext null

        val gravador = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            TAXA,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            max(tamanhoMinimo, TAXA * 2)
        )
        if (gravador.state != AudioRecord.STATE_INITIALIZED) {
            gravador.release()
            return@withContext null
        }

        try {
            val amostras = ShortArray((TAXA * duracaoMs / 1000L).toInt())
            gravador.startRecording()
            var lidas = 0
            while (lidas < amostras.size) {
                val n = gravador.read(amostras, lidas, amostras.size - lidas)
                if (n <= 0) break
                lidas += n
            }
            calcular(amostras.copyOf(lidas))
        } finally {
            runCatching { gravador.stop() }
            gravador.release()
        }
    }

    private fun calcular(amostras: ShortArray): MetricasVoz? {
        val sinal = FloatArray(amostras.size) { amostras[it] / 32768f }
        val quadros = sinal.size / TAMANHO_QUADRO
        if (quadros < 2) return null

        val rmsQuadros = FloatArray(quadros) { indice ->
            var soma = 0f
            val inicio = indice * TAMANHO_QUADRO
            for (k in inicio until inicio + TAMANHO_QUADRO) soma += sinal[k] * sinal[k]
            sqrt(soma / TAMANHO_QUADRO)
        }
        val pico = rmsQuadros.max()
        if (pico < LIMITE_SILENCIO_ABSOLUTO) return null

        val limiar = max(LIMITE_SILENCIO_ABSOLUTO, pico * FRACAO_LIMIAR_VOZ)
        val vozeado = BooleanArray(quadros) { rmsQuadros[it] > limiar }
        val primeiro = vozeado.indexOfFirst { it }
        val ultimo = vozeado.indexOfLast { it }
        if (primeiro < 0) return null

        val pausas = contarPausas(vozeado, primeiro, ultimo)
        val duracaoFalaSegundos = (ultimo - primeiro + 1) * SEGUNDOS_POR_QUADRO
        val silabas = contarPicosEnergia(rmsQuadros, vozeado, primeiro, ultimo, limiar)
        val velocidade = if (duracaoFalaSegundos > 0f) silabas / duracaoFalaSegundos else 0f

        var somaQuadrados = 0.0
        for (valor in sinal) somaQuadrados += valor * valor
        val volumeTotal = sqrt((somaQuadrados / sinal.size).toFloat())
        val volumeDb = 20f * log10(max(volumeTotal, 1e-6f))

        return MetricasVoz(
            volumeDb = volumeDb,
            variacaoTomSemitons = variacaoTom(sinal, vozeado),
            velocidadeSilabasPorSegundo = velocidade,
            numeroPausas = pausas,
            duracaoSegundos = sinal.size.toFloat() / TAXA
        )
    }

    private fun contarPausas(vozeado: BooleanArray, primeiro: Int, ultimo: Int): Int {
        var pausas = 0
        var silencioConsecutivo = 0
        for (i in primeiro..ultimo) {
            if (vozeado[i]) {
                if (silencioConsecutivo >= QUADROS_MINIMOS_PAUSA) pausas++
                silencioConsecutivo = 0
            } else {
                silencioConsecutivo++
            }
        }
        return pausas
    }

    private fun contarPicosEnergia(
        rms: FloatArray,
        vozeado: BooleanArray,
        primeiro: Int,
        ultimo: Int,
        limiar: Float
    ): Int {
        val suavizado = FloatArray(rms.size) { i ->
            val a = max(0, i - 1)
            val b = minOf(rms.size - 1, i + 1)
            (rms[a] + rms[i] + rms[b]) / (b - a + 1)
        }
        var picos = 0
        var ultimoPico = -DISTANCIA_MINIMA_PICOS
        for (i in primeiro + 1 until ultimo) {
            val pico = vozeado[i] &&
                suavizado[i] > limiar * FATOR_PICO_SILABA &&
                suavizado[i] > suavizado[i - 1] &&
                suavizado[i] >= suavizado[i + 1]
            if (pico && i - ultimoPico >= DISTANCIA_MINIMA_PICOS) {
                picos++
                ultimoPico = i
            }
        }
        return picos
    }

    private fun variacaoTom(sinal: FloatArray, vozeado: BooleanArray): Float {
        val frequencias = mutableListOf<Float>()
        for (quadro in vozeado.indices) {
            if (!vozeado[quadro]) continue
            val inicio = quadro * TAMANHO_QUADRO + TAMANHO_QUADRO / 2 - JANELA_TOM / 2
            if (inicio < 0 || inicio + JANELA_TOM + LAG_MAXIMO >= sinal.size) continue
            val lag = melhorLag(sinal, inicio) ?: continue
            frequencias += TAXA.toFloat() / lag
        }
        if (frequencias.size < MINIMO_AMOSTRAS_TOM) return 0f

        val ordenadas = frequencias.sorted()
        val referencia = ordenadas[ordenadas.size / 2]
        val semitons = frequencias.map { 12f * log2(it / referencia) }
        val media = semitons.average().toFloat()
        val variancia = semitons.map { (it - media) * (it - media) }.average().toFloat()
        return sqrt(variancia)
    }

    private fun melhorLag(sinal: FloatArray, inicio: Int): Int? {
        var melhorLag: Int? = null
        var melhorCorrelacao = LIMITE_CORRELACAO_TOM
        for (lag in LAG_MINIMO..LAG_MAXIMO) {
            var produto = 0f
            var energia1 = 0f
            var energia2 = 0f
            for (k in 0 until JANELA_TOM) {
                val a = sinal[inicio + k]
                val b = sinal[inicio + k + lag]
                produto += a * b
                energia1 += a * a
                energia2 += b * b
            }
            val denominador = sqrt(energia1 * energia2)
            if (denominador <= 0f) continue
            val correlacao = produto / denominador
            if (correlacao > melhorCorrelacao) {
                melhorCorrelacao = correlacao
                melhorLag = lag
            }
        }
        return melhorLag
    }

    private companion object {
        const val TAXA = 16000
        const val DURACAO_PADRAO_MS = 8000L
        const val TAMANHO_QUADRO = 320
        const val SEGUNDOS_POR_QUADRO = 0.02f
        const val LIMITE_SILENCIO_ABSOLUTO = 0.005f
        const val FRACAO_LIMIAR_VOZ = 0.2f
        const val QUADROS_MINIMOS_PAUSA = 15
        const val DISTANCIA_MINIMA_PICOS = 5
        const val FATOR_PICO_SILABA = 1.5f
        const val JANELA_TOM = 480
        const val LAG_MINIMO = 40
        const val LAG_MAXIMO = 213
        const val LIMITE_CORRELACAO_TOM = 0.6f
        const val MINIMO_AMOSTRAS_TOM = 5
    }
}
