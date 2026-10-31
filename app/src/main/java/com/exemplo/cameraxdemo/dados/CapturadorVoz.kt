package com.exemplo.cameraxdemo.dados

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.exemplo.cameraxdemo.dominio.CalculadoraVoz
import com.exemplo.cameraxdemo.dominio.MetricasVoz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

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
            CalculadoraVoz.calcular(amostras.copyOf(lidas))
        } finally {
            runCatching { gravador.stop() }
            gravador.release()
        }
    }

    private companion object {
        const val TAXA = 16000
        const val DURACAO_PADRAO_MS = 8000L
    }
}
