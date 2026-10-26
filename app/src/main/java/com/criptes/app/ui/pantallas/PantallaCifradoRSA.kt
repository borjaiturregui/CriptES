package com.criptes.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criptes.app.criptografia.CifradoRSA
import com.criptes.app.criptografia.ResultadoRSA
import com.criptes.app.ui.tema.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ============================================================
//  CriptES — Pantalla de Cifrado Asimétrico RSA
//  Generar claves, cifrar con la pública, descifrar con la privada
// ============================================================

data class EstadoRSA(
    val modo:          Int     = MODO_CLAVES,
    val clavePublica:  String  = "",
    val clavePrivada:  String  = "",
    val texto:         String  = "",
    val resultado:     String  = "",
    val error:         String? = null,
    val estaCargando:  Boolean = false
) {
    companion object {
        const val MODO_CLAVES    = 0
        const val MODO_CIFRAR    = 1
        const val MODO_DESCIFRAR = 2
    }
}

class ViewModelCifradoRSA : ViewModel() {

    private val _estado = MutableStateFlow(EstadoRSA())
    val estado: StateFlow<EstadoRSA> = _estado.asStateFlow()

    fun cambiarModo(modo: Int)            { _estado.value = _estado.value.copy(modo = modo, texto = "", resultado = "", error = null) }
    fun actualizarTexto(texto: String)    { _estado.value = _estado.value.copy(texto = texto, resultado = "", error = null) }
    fun actualizarClavePublica(k: String) { _estado.value = _estado.value.copy(clavePublica = k, resultado = "", error = null) }
    fun actualizarClavePrivada(k: String) { _estado.value = _estado.value.copy(clavePrivada = k, resultado = "", error = null) }

    fun ejecutar() {
        val e = _estado.value
        _estado.value = e.copy(estaCargando = true, error = null, resultado = "")

        viewModelScope.launch(Dispatchers.Default) {
            val r: ResultadoRSA = when (e.modo) {
                EstadoRSA.MODO_CLAVES -> CifradoRSA.generarParClaves()
                EstadoRSA.MODO_CIFRAR -> CifradoRSA.cifrar(e.texto, e.clavePublica)
                else                  -> CifradoRSA.descifrar(e.texto, e.clavePrivada)
            }
            val actual = _estado.value
            _estado.value = when (r) {
                is ResultadoRSA.ExitoClaves -> actual.copy(
                    clavePublica = r.parClaves.clavePublicaPEM,
                    clavePrivada = r.parClaves.clavePrivadaPEM,
                    estaCargando = false
                )
                is ResultadoRSA.ExitoCifrado    -> actual.copy(resultado = r.textoCifrado, estaCargando = false)
                is ResultadoRSA.ExitoDescifrado -> actual.copy(resultado = r.textoPlano,   estaCargando = false)
                is ResultadoRSA.Error           -> actual.copy(error = r.mensaje,          estaCargando = false)
            }
        }
    }
}

@Composable
fun PantallaCifradoRSA(
    viewModel:      ViewModelCifradoRSA = androidx.lifecycle.viewmodel.compose.viewModel(),
    alRetroceder:   () -> Unit,
    alVerEducativo: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val portapapeles = LocalClipboardManager.current

    Scaffold(
        topBar = { BarraSuperiorCriptES("Cifrado RSA", "RSA", alRetroceder, alVerEducativo) },
        containerColor = NegroPuro
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SelectorSegmentado(
                opciones     = listOf("🔑 Claves", "🔒 Cifrar", "🔓 Descifrar"),
                seleccionada = estado.modo,
                alCambiar    = viewModel::cambiarModo
            )

            when (estado.modo) {
                EstadoRSA.MODO_CLAVES -> {
                    Text(
                        "Genera un par de claves RSA de 2048 bits. Comparte la pública; guarda la privada.",
                        style = MaterialTheme.typography.bodySmall, color = GrisClaro
                    )
                    BotonPrincipal("🔑 Generar par de claves", estado.estaCargando, viewModel::ejecutar)
                    if (estado.clavePrivada.isNotEmpty()) {
                        TarjetaAviso(
                            "La clave privada no se guarda en ningún sitio: si cierras la app se pierde. " +
                            "Cópiala a un lugar seguro y no la compartas nunca."
                        )
                        TarjetaTextoCopiable("Clave pública (PEM)", estado.clavePublica) {
                            portapapeles.setText(AnnotatedString(estado.clavePublica))
                        }
                        TarjetaTextoCopiable("Clave privada (PEM)", estado.clavePrivada) {
                            portapapeles.setText(AnnotatedString(estado.clavePrivada))
                        }
                    }
                }

                EstadoRSA.MODO_CIFRAR -> {
                    CampoTextoEstilizado(
                        valor = estado.clavePublica, etiqueta = "Clave pública (PEM)",
                        marcador = "-----BEGIN PUBLIC KEY-----", onCambio = viewModel::actualizarClavePublica
                    )
                    CampoTextoEstilizado(
                        valor = estado.texto, etiqueta = "Texto a cifrar",
                        marcador = "Máx. 190 bytes (≈190 caracteres sin tildes)",
                        onCambio = viewModel::actualizarTexto
                    )
                    BotonPrincipal("🔒 Cifrar", estado.estaCargando, viewModel::ejecutar)
                }

                else -> {
                    CampoTextoEstilizado(
                        valor = estado.clavePrivada, etiqueta = "Clave privada (PEM)",
                        marcador = "-----BEGIN PRIVATE KEY-----", onCambio = viewModel::actualizarClavePrivada
                    )
                    CampoTextoEstilizado(
                        valor = estado.texto, etiqueta = "Texto cifrado (Base64)",
                        marcador = "Pega el texto cifrado aquí...", onCambio = viewModel::actualizarTexto
                    )
                    BotonPrincipal("🔓 Descifrar", estado.estaCargando, viewModel::ejecutar)
                }
            }

            estado.error?.let { TarjetaError(mensaje = it) }

            if (estado.resultado.isNotEmpty()) {
                TarjetaResultado(
                    resultado = estado.resultado,
                    esExito   = true,
                    alCopiar  = { portapapeles.setText(AnnotatedString(estado.resultado)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
