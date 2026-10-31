package com.exemplo.cameraxdemo.apresentacao

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemplo.cameraxdemo.BuildConfig
import com.exemplo.cameraxdemo.dados.AnalisadorQuadros
import com.exemplo.cameraxdemo.dados.CapturadorVoz
import com.exemplo.cameraxdemo.dados.ClienteAnaliseApi
import com.exemplo.cameraxdemo.dados.ControladorCamera
import com.exemplo.cameraxdemo.dados.MetricasPoseDto
import com.exemplo.cameraxdemo.dados.MetricasVozDto
import com.exemplo.cameraxdemo.dados.PedidoAnalise
import com.exemplo.cameraxdemo.dados.ParserRespostaAnalise
import com.exemplo.cameraxdemo.dados.ProcessadorLuminancia
import com.exemplo.cameraxdemo.dados.ProcessadorPose
import com.exemplo.cameraxdemo.dados.ProcessadorQuadroCompleto
import com.exemplo.cameraxdemo.dominio.ModoAnalise
import com.exemplo.cameraxdemo.dominio.ResultadoAnalise
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CameraViewModel(aplicacao: Application) : AndroidViewModel(aplicacao) {

    private val _estado = MutableStateFlow(EstadoCamera())
    val estado: StateFlow<EstadoCamera> = _estado.asStateFlow()

    private val _analise = MutableStateFlow<EstadoAnalise>(EstadoAnalise.Ocioso)
    val analise: StateFlow<EstadoAnalise> = _analise.asStateFlow()

    private val processadorPose = ProcessadorPose()

    val analisador = AnalisadorQuadros(
        ProcessadorQuadroCompleto(ProcessadorLuminancia(), processadorPose)
    ) { resultado, fps ->
        _estado.update {
            it.copy(
                largura = resultado.largura,
                altura = resultado.altura,
                rotacao = resultado.rotacao,
                luminanciaMedia = resultado.luminanciaMedia,
                fps = fps,
                recebendoQuadros = true,
                metricasPose = resultado.metricasPose
            )
        }
    }

    val controladorCamera = ControladorCamera(aplicacao, analisador)

    private val capturadorVoz = CapturadorVoz()
    private val clienteApi = ClienteAnaliseApi(BuildConfig.URL_API)
    private val parser = ParserRespostaAnalise()

    fun analisar(modo: ModoAnalise) {
        if (_analise.value is EstadoAnalise.Carregando) return
        viewModelScope.launch {
            try {
                val precisaVoz = modo == ModoAnalise.VOZ || modo == ModoAnalise.COMPLETO
                if (precisaVoz && !microfoneDisponivel()) {
                    _analise.value = EstadoAnalise.Erro(MENSAGEM_SEM_MICROFONE)
                    return@launch
                }

                _analise.value = EstadoAnalise.Carregando(
                    if (precisaVoz) "Gravando voz e capturando imagem..." else "Capturando imagem..."
                )

                val imagem = if (modo != ModoAnalise.VOZ) controladorCamera.capturarQuadroBase64() else null
                val precisaPose = modo == ModoAnalise.POSE || modo == ModoAnalise.COMPLETO
                val metricasPose = if (precisaPose) _estado.value.metricasPose else null
                val metricasVoz = if (precisaVoz) capturadorVoz.capturar() else null

                _analise.value = EstadoAnalise.Carregando("Processando análise no servidor...")

                val pedido = PedidoAnalise(
                    modo = modo.valor,
                    imagemJpegBase64 = imagem,
                    metricasPose = metricasPose?.let {
                        MetricasPoseDto(
                            inclinacaoOmbrosGraus = it.inclinacaoOmbrosGraus,
                            deslocamentoCabecaRazao = it.deslocamentoCabecaRazao,
                            bracosCruzados = it.bracosCruzados
                        )
                    },
                    metricasVoz = metricasVoz?.let {
                        MetricasVozDto(
                            volumeDb = it.volumeDb,
                            variacaoTomSemitons = it.variacaoTomSemitons,
                            velocidadeSilabasPorSegundo = it.velocidadeSilabasPorSegundo,
                            numeroPausas = it.numeroPausas,
                            duracaoSegundos = it.duracaoSegundos
                        )
                    }
                )

                val corpo = clienteApi.analisar(pedido)
                _analise.value = when (val resultado = parser.interpretar(corpo)) {
                    is ResultadoAnalise.Sucesso -> EstadoAnalise.Sucesso(
                        pose = resultado.pose,
                        roupa = resultado.roupa,
                        voz = resultado.voz,
                        modoDemonstracao = resultado.modoDemonstracao
                    )
                    ResultadoAnalise.Recusado -> EstadoAnalise.Recusado
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: ConnectException) {
                _analise.value = EstadoAnalise.Erro("Servidor indisponível (${BuildConfig.URL_API}). Verifique se o backend Node.js está rodando.")
            } catch (_: UnknownHostException) {
                _analise.value = EstadoAnalise.Erro("Endereço do servidor não encontrado. Verifique as configurações de rede.")
            } catch (_: SocketTimeoutException) {
                _analise.value = EstadoAnalise.Erro("Tempo limite excedido ao aguardar a resposta. Tente novamente.")
            } catch (e: IOException) {
                _analise.value = EstadoAnalise.Erro("Falha na comunicação: ${e.localizedMessage}")
            } catch (e: Exception) {
                _analise.value = EstadoAnalise.Erro(e.localizedMessage ?: MENSAGEM_ERRO)
            }
        }
    }

    private fun microfoneDisponivel(): Boolean =
        ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCleared() {
        controladorCamera.liberar()
        processadorPose.close()
        super.onCleared()
    }
}
