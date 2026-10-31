package com.exemplo.cameraxdemo.apresentacao

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.exemplo.cameraxdemo.dados.ControladorCamera

@Composable
fun PreviewCamera(
    controlador: ControladorCamera,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(contexto).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    LaunchedEffect(controlador, previewView, ciclo) {
        controlador.vincular(ciclo, previewView.surfaceProvider)
    }

    DisposableEffect(controlador) {
        onDispose { controlador.desvincular() }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
