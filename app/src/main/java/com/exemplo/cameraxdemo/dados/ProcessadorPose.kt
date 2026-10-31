package com.exemplo.cameraxdemo.dados

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.exemplo.cameraxdemo.dominio.CalculadoraPose
import com.exemplo.cameraxdemo.dominio.MetricasPose
import com.exemplo.cameraxdemo.dominio.PontoCorpo
import com.exemplo.cameraxdemo.dominio.TipoPonto
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.google.mlkit.vision.pose.PoseLandmark

class ProcessadorPose : AutoCloseable {

    private val detector: PoseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    fun processar(imagem: ImageProxy): MetricasPose? {
        val midia = imagem.image ?: return null
        return runCatching {
            val entrada = InputImage.fromMediaImage(midia, imagem.imageInfo.rotationDegrees)
            val pose = Tasks.await(detector.process(entrada))
            CalculadoraPose.calcular(mapear(pose))
        }.getOrNull()
    }

    private fun mapear(pose: Pose): Map<TipoPonto, PontoCorpo> {
        val mapa = linkedMapOf<TipoPonto, PontoCorpo>()
        val correspondencias = mapOf(
            TipoPonto.ORELHA_ESQUERDA to PoseLandmark.LEFT_EAR,
            TipoPonto.ORELHA_DIREITA to PoseLandmark.RIGHT_EAR,
            TipoPonto.OMBRO_ESQUERDO to PoseLandmark.LEFT_SHOULDER,
            TipoPonto.OMBRO_DIREITO to PoseLandmark.RIGHT_SHOULDER,
            TipoPonto.COTOVELO_ESQUERDO to PoseLandmark.LEFT_ELBOW,
            TipoPonto.COTOVELO_DIREITO to PoseLandmark.RIGHT_ELBOW,
            TipoPonto.PULSO_ESQUERDO to PoseLandmark.LEFT_WRIST,
            TipoPonto.PULSO_DIREITO to PoseLandmark.RIGHT_WRIST
        )
        correspondencias.forEach { (tipo, tipoMlKit) ->
            pose.getPoseLandmark(tipoMlKit)?.let { landmark ->
                mapa[tipo] = PontoCorpo(
                    x = landmark.position.x,
                    y = landmark.position.y,
                    confianca = landmark.inFrameLikelihood
                )
            }
        }
        return mapa
    }

    override fun close() {
        detector.close()
    }
}
