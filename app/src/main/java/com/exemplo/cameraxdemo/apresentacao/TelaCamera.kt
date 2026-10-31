package com.exemplo.cameraxdemo.apresentacao

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TelaCamera(viewModel: CameraViewModel = viewModel()) {
    val contexto = LocalContext.current

    var consentimentoAceito by rememberSaveable { mutableStateOf(false) }
    var permissaoCamera by remember { mutableStateOf(temPermissao(contexto, Manifest.permission.CAMERA)) }

    val solicitador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        permissaoCamera = resultado[Manifest.permission.CAMERA] == true
    }

    val solicitarPermissoes = {
        solicitador.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    when {
        !consentimentoAceito -> TelaConsentimento(
            onAceitar = {
                consentimentoAceito = true
                solicitarPermissoes()
            }
        )
        permissaoCamera -> ConteudoCamera(viewModel)
        else -> SemPermissaoCamera(onConceder = solicitarPermissoes)
    }
}

private fun temPermissao(contexto: Context, permissao: String): Boolean =
    ContextCompat.checkSelfPermission(contexto, permissao) == PackageManager.PERMISSION_GRANTED

@Composable
private fun SemPermissaoCamera(onConceder: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "A permissão da câmera é necessária")
        Button(
            onClick = onConceder,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Conceder permissão")
        }
    }
}

@Composable
private fun ConteudoCamera(viewModel: CameraViewModel) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val analise by viewModel.analise.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        PreviewCamera(
            controlador = viewModel.controladorCamera,
            modifier = Modifier.fillMaxSize()
        )
        PainelMetricas(
            estado = estado,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
        PainelAcoes(
            analise = analise,
            onAnalisar = viewModel::analisar,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PainelMetricas(estado: EstadoCamera, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val estilo = MaterialTheme.typography.bodyMedium
        val cor = Color.White
        if (estado.recebendoQuadros) {
            Text("Resolução: ${estado.largura}x${estado.altura}", style = estilo, color = cor)
            Text("Rotação: ${estado.rotacao}°", style = estilo, color = cor)
            Text("Luminância: ${estado.luminanciaMedia}/255", style = estilo, color = cor)
            Text("FPS: ${"%.1f".format(estado.fps)}", style = estilo, color = cor)
        } else {
            Text("Aguardando quadros...", style = estilo, color = cor)
        }
    }
}
