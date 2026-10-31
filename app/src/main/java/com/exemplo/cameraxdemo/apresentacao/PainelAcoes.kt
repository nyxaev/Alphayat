package com.exemplo.cameraxdemo.apresentacao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exemplo.cameraxdemo.dominio.ModoAnalise

@Composable
fun PainelAcoes(
    analise: EstadoAnalise,
    onAnalisar: (ModoAnalise) -> Unit,
    modifier: Modifier = Modifier
) {
    val ocupado = analise is EstadoAnalise.Carregando

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.75f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AreaResultado(analise)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onAnalisar(ModoAnalise.POSE) },
                enabled = !ocupado,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Analisar pose")
            }
            Button(
                onClick = { onAnalisar(ModoAnalise.ROUPA) },
                enabled = !ocupado,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Analisar roupa")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onAnalisar(ModoAnalise.VOZ) },
                enabled = !ocupado,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Analisar voz")
            }
            Button(
                onClick = { onAnalisar(ModoAnalise.COMPLETO) },
                enabled = !ocupado,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Análise completa")
            }
        }
    }
}

@Composable
private fun AreaResultado(analise: EstadoAnalise) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 260.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (analise) {
            EstadoAnalise.Ocioso -> TextoResultado("Escolha uma análise abaixo.")
            is EstadoAnalise.Carregando -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextoResultado(analise.mensagem)
            }
            is EstadoAnalise.Sucesso -> {
                if (analise.modoDemonstracao) {
                    Text(
                        text = "ℹ️ Modo Demonstrativo (Análise Local)",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
                if (analise.pose == null && analise.roupa == null && analise.voz == null) {
                    TextoResultado("Nada a comentar para esta análise.")
                }
                analise.pose?.let { Secao("Postura", it) }
                analise.roupa?.let { Secao("Roupa", it) }
                analise.voz?.let { Secao("Voz", it) }
            }
            EstadoAnalise.Recusado -> TextoResultado(MENSAGEM_RECUSA)
            is EstadoAnalise.Erro -> TextoResultado(analise.mensagem)
        }
    }
}

@Composable
private fun Secao(titulo: String, texto: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = titulo, color = Color.White, fontWeight = FontWeight.Bold)
        Text(text = texto, color = Color.White)
    }
}

@Composable
private fun TextoResultado(texto: String) {
    Text(text = texto, color = Color.White)
}
