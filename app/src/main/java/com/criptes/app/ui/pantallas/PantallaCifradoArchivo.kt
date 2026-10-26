package com.criptes.app.ui.pantallas

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criptes.app.criptografia.CifradoArchivos
import com.criptes.app.criptografia.ResultadoArchivo
import com.criptes.app.ui.tema.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.IOException

// ============================================================
//  CriptES — Pantalla de Cifrado de Archivos
//  AES-256-GCM por bloques; el usuario elige origen y destino
//  con el selector de documentos del sistema (sin permisos de almacenamiento)
// ============================================================

data class EstadoCifradoArchivo(
    val modo:          Int     = MODO_CIFRAR,
    val uriEntrada:    Uri?    = null,
    val nombreEntrada: String  = "",
    val contrasena:    String  = "",
    val confirmacion:  String  = "",
    val error:         String? = null,
    val info:          String? = null,
    val estaCargando:  Boolean = false
) {
    companion object {
        const val MODO_CIFRAR    = 0
        const val MODO_DESCIFRAR = 1
    }
}

class ViewModelCifradoArchivo : ViewModel() {

    private val _estado = MutableStateFlow(EstadoCifradoArchivo())
    val estado: StateFlow<EstadoCifradoArchivo> = _estado.asStateFlow()

    fun cambiarModo(modo: Int) { _estado.value = EstadoCifradoArchivo(modo = modo) }
    fun actualizarContrasena(c: String)  { _estado.value = _estado.value.copy(contrasena = c, error = null, info = null) }
    fun actualizarConfirmacion(c: String) { _estado.value = _estado.value.copy(confirmacion = c, error = null, info = null) }

    fun elegirEntrada(uri: Uri, nombre: String?) {
        _estado.value = _estado.value.copy(uriEntrada = uri, nombreEntrada = nombre ?: "archivo", error = null, info = null)
    }

    /**
     * Valida los datos y devuelve el nombre sugerido para el archivo de salida,
     * o null (con el error ya publicado) si falta algo.
     */
    fun nombreSalida(): String? {
        val e = _estado.value
        val error = when {
            e.uriEntrada == null      -> "Elige primero un archivo"
            e.contrasena.isBlank()    -> "La contraseña no puede estar vacía"
            e.modo == EstadoCifradoArchivo.MODO_CIFRAR && e.contrasena != e.confirmacion ->
                "Las contraseñas no coinciden. Si te equivocas al escribirla, no podrás recuperar el archivo"
            else -> null
        }
        if (error != null) { _estado.value = e.copy(error = error); return null }
        return if (e.modo == EstadoCifradoArchivo.MODO_CIFRAR) CifradoArchivos.nombreCifrado(e.nombreEntrada)
               else CifradoArchivos.nombreDescifrado(e.nombreEntrada)
    }

    fun procesar(resolver: ContentResolver, uriSalida: Uri) {
        val e = _estado.value
        val uriEntrada = e.uriEntrada ?: return
        _estado.value = e.copy(estaCargando = true, error = null, info = null)

        viewModelScope.launch(Dispatchers.IO) {
            val resultado: ResultadoArchivo = try {
                val entrada = resolver.openInputStream(uriEntrada) ?: throw IOException("No se pudo abrir el archivo de origen")
                val salida  = resolver.openOutputStream(uriSalida, "wt") ?: throw IOException("No se pudo abrir el destino")
                entrada.use { ins ->
                    salida.use { outs ->
                        val buffer = BufferedInputStream(ins, 64 * 1024)
                        if (e.modo == EstadoCifradoArchivo.MODO_CIFRAR)
                            CifradoArchivos.cifrar(buffer, outs, e.contrasena)
                        else
                            CifradoArchivos.descifrar(buffer, outs, e.contrasena)
                    }
                }
            } catch (ex: Exception) {
                ResultadoArchivo.Error("No se pudo leer o escribir el archivo: ${ex.localizedMessage}")
            }

            when (resultado) {
                is ResultadoArchivo.Exito -> _estado.value = _estado.value.copy(
                    estaCargando = false,
                    info = if (e.modo == EstadoCifradoArchivo.MODO_CIFRAR)
                        "Archivo cifrado y guardado (${resultado.bytesProcesados} bytes). Sin la contraseña no se puede recuperar."
                    else
                        "Archivo descifrado y guardado (${resultado.bytesProcesados} bytes)."
                )
                is ResultadoArchivo.Error -> {
                    // No dejar en el destino un archivo vacío o a medias
                    try { DocumentsContract.deleteDocument(resolver, uriSalida) } catch (ignorada: Exception) {}
                    _estado.value = _estado.value.copy(estaCargando = false, error = resultado.mensaje)
                }
            }
        }
    }
}

@Composable
fun PantallaCifradoArchivo(
    viewModel:      ViewModelCifradoArchivo = androidx.lifecycle.viewmodel.compose.viewModel(),
    alRetroceder:   () -> Unit,
    alVerEducativo: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current
    val cifrando = estado.modo == EstadoCifradoArchivo.MODO_CIFRAR

    val elegirArchivo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.elegirEntrada(uri, nombreDeUri(contexto, uri))
    }
    val elegirDestino = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) viewModel.procesar(contexto.contentResolver, uri)
    }

    Scaffold(
        topBar = { BarraSuperiorCriptES("Cifrado de Archivos", "AES", alRetroceder, alVerEducativo) },
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
                opciones     = listOf("🔒 Cifrar", "🔓 Descifrar"),
                seleccionada = estado.modo,
                alCambiar    = viewModel::cambiarModo
            )

            Text(
                if (cifrando) "AES-256-GCM por bloques: detecta cualquier modificación del archivo. Se guarda con extensión .criptes."
                else "Elige un archivo .criptes y la contraseña con la que se cifró.",
                style = MaterialTheme.typography.bodySmall, color = GrisClaro
            )

            OutlinedButton(
                onClick  = { elegirArchivo.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                enabled  = !estado.estaCargando
            ) {
                Text(
                    if (estado.uriEntrada == null) "Elegir archivo" else "Archivo: ${estado.nombreEntrada}",
                    color = RojoVino400
                )
            }

            CampoContrasena(valor = estado.contrasena, onCambio = viewModel::actualizarContrasena)
            if (cifrando) {
                CampoContrasena(
                    valor = estado.confirmacion, onCambio = viewModel::actualizarConfirmacion,
                    etiqueta = "Repite la contraseña"
                )
                TarjetaAviso("No hay forma de recuperar un archivo si olvidas la contraseña.")
            }

            BotonPrincipal(
                texto        = if (cifrando) "🔒 Cifrar y guardar" else "🔓 Descifrar y guardar",
                estaCargando = estado.estaCargando,
                alClick      = { viewModel.nombreSalida()?.let { elegirDestino.launch(it) } }
            )

            estado.error?.let { TarjetaError(mensaje = it) }
            estado.info?.let { TarjetaInfo(mensaje = it) }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
