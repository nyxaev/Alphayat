package com.exemplo.cameraxdemo.dados

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ControladorCamera(
    private val contexto: Context,
    private val analisador: ImageAnalysis.Analyzer
) {

    private val executorAnalise: ExecutorService = Executors.newSingleThreadExecutor()
    private val executorCaptura: ExecutorService = Executors.newSingleThreadExecutor()
    private var provedorCamera: ProcessCameraProvider? = null
    private var captura: ImageCapture? = null

    suspend fun vincular(
        ciclo: LifecycleOwner,
        superficie: Preview.SurfaceProvider
    ) {
        val provedor = ProcessCameraProvider.awaitInstance(contexto)
        provedorCamera = provedor

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(superficie)
        }

        val analise = ImageAnalysis.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(640, 480),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()
            .also { it.setAnalyzer(executorAnalise, analisador) }

        val capturaImagem = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        captura = capturaImagem

        provedor.unbindAll()
        provedor.bindToLifecycle(
            ciclo,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analise,
            capturaImagem
        )
    }

    suspend fun capturarQuadroBase64(): String = suspendCancellableCoroutine { continuacao ->
        val alvo = captura
        if (alvo == null) {
            continuacao.resumeWithException(IllegalStateException("A câmera ainda não está pronta"))
            return@suspendCancellableCoroutine
        }
        alvo.takePicture(executorCaptura, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imagem: ImageProxy) {
                runCatching { CodificadorImagem.paraBase64(imagem) }
                    .onSuccess { continuacao.resume(it) }
                    .onFailure { continuacao.resumeWithException(it) }
            }

            override fun onError(exception: ImageCaptureException) {
                continuacao.resumeWithException(exception)
            }
        })
    }

    fun desvincular() {
        provedorCamera?.unbindAll()
        provedorCamera = null
        captura = null
    }

    fun liberar() {
        desvincular()
        executorAnalise.shutdown()
        executorCaptura.shutdown()
    }
}
