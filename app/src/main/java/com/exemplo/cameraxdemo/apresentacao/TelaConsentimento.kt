package com.exemplo.cameraxdemo.apresentacao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TelaConsentimento(onAceitar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Antes de começar",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Este app usa a câmera e o microfone somente quando você pede uma análise. " +
                "Para analisar, um quadro da câmera e medidas da sua voz são enviados ao servidor e " +
                "usados apenas para gerar o feedback. O áudio bruto nunca sai do aparelho. " +
                "Nada é armazenado: o app não grava imagens nem áudios, e o servidor não guarda os dados da análise. " +
                "Você pode retirar as permissões a qualquer momento nas configurações do aparelho.",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = onAceitar,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(text = "Aceitar e continuar")
        }
    }
}
