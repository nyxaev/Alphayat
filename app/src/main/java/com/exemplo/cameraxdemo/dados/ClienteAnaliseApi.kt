package com.exemplo.cameraxdemo.dados

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class ClienteAnaliseApi(
    private val url: String,
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    private val json = Json { explicitNulls = true }

    suspend fun analisar(pedido: PedidoAnalise): String = withContext(Dispatchers.IO) {
        val corpo = json.encodeToString(PedidoAnalise.serializer(), pedido)
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val requisicao = Request.Builder()
            .url(url)
            .post(corpo)
            .build()
        http.newCall(requisicao).execute().use { resposta ->
            if (!resposta.isSuccessful) throw IOException("Resposta HTTP ${resposta.code}")
            resposta.body?.string() ?: throw IOException("Resposta vazia")
        }
    }
}
