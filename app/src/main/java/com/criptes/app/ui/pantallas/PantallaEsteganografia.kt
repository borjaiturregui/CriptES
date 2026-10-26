package com.criptes.app.ui.pantallas

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criptes.app.criptografia.Esteganografia
import com.criptes.app.criptografia.ResultadoEsteganografia
import com.criptes.app.ui.tema.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ============================================================
//  CriptES — Pantalla de Esteganografía LSB
//  Ocultar y extraer mensajes en imágenes PNG
// ============================================================

data class EstadoEsteganografia(
    val modo:            Int      = MODO_OCULTAR,
    val imagen:          Bitmap?  = null,
    val nombreImagen:    String   = "",
    val mensaje:         String   = "",
    val contrasena:      String   = "",
    val imagenResultado: Bitmap?  = null,
    val mensajeExtraido: String   = "",
    val error:           String?  = null,
    val info:            String?  = null,
    val estaCargando:    Boolean  = false
) {
    companion object {
        const val MODO_OCULTAR = 0
        const val MODO_EXTRAER = 1
    }
}

class ViewModelEsteganografia : ViewModel() {

    private companion object {
        /** Límite de píxeles para no agotar la memoria (≈100 MB de trabajo). */
        const val MAX_PIXELES = 8_000_000L
    }

    private val _estado = MutableStateFlow(EstadoEsteganografia())
    val estado: StateFlow<EstadoEsteganografia> = _estado.asStateFlow()

    fun cambiarModo(modo: Int) { _estado.value = EstadoEsteganografia(modo = modo) }
    fun actualizarMensaje(m: String)    { _estado.value = _estado.value.copy(mensaje = m, imagenResultado = null, info = null, error = null) }
    fun actualizarContrasena(c: String) { _estado.value = _estado.value.copy(contrasena = c, imagenResultado = null, mensajeExtraido = "", info = null, error = null) }

    fun cargarImagen(resolver: ContentResolver, uri: Uri, nombre: String?) {
        val modo = _estado.value.modo
        _estado.value = _estado.value.copy(estaCargando = true, error = null, info = null, imagenResultado = null, mensajeExtraido = "")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
                if (limites.outWidth <= 0 || limites.outHeight <= 0) {
                    fallar("No se pudo leer la imagen"); return@launch
                }
                val pixeles = limites.outWidth.toLong() * limites.outHeight
                var muestreo = 1
                while (pixeles / (muestreo.toLong() * muestreo) > MAX_PIXELES) muestreo *= 2
                if (muestreo > 1 && modo == EstadoEsteganografia.MODO_EXTRAER) {
                    // Reducir la imagen destruiría el mensaje: mejor rechazarla.
                    fallar("Imagen demasiado grande para extraer (máx. ${MAX_PIXELES / 1_000_000} megapíxeles)")
                    return@launch
                }
                val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
                val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
                if (bitmap == null) { fallar("No se pudo leer la imagen"); return@launch }
                _estado.value = _estado.value.copy(
                    imagen        = bitmap,
                    nombreImagen  = nombre ?: "imagen",
                    estaCargando  = false,
                    info          = if (muestreo > 1) "La imagen era muy grande y se ha reducido para poder procesarla." else null
                )
            } catch (e: OutOfMemoryError) {
                fallar("Imagen demasiado grande para este dispositivo")
            } catch (e: Exception) {
                fallar("No se pudo leer la imagen: ${e.localizedMessage}")
            }
        }
    }

    fun ejecutar() {
        val e = _estado.value
        val imagen = e.imagen
        if (imagen == null) { _estado.value = e.copy(error = "Elige primero una imagen"); return }
        if (e.modo == EstadoEsteganografia.MODO_OCULTAR && e.mensaje.isBlank()) {
            _estado.value = e.copy(error = "Escribe el mensaje que quieres ocultar"); return
        }
        _estado.value = e.copy(estaCargando = true, error = null, info = null, imagenResultado = null, mensajeExtraido = "")
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val r = if (e.modo == EstadoEsteganografia.MODO_OCULTAR)
                    Esteganografia.ocultarMensaje(imagen, e.mensaje, e.contrasena)
                else
                    Esteganografia.extraerMensaje(imagen, e.contrasena)
                val actual = _estado.value
                _estado.value = when (r) {
                    is ResultadoEsteganografia.ExitoOcultar -> actual.copy(imagenResultado = r.imagenModificada, estaCargando = false)
                    is ResultadoEsteganografia.ExitoExtraer -> actual.copy(mensajeExtraido = r.mensajeOculto, estaCargando = false)
                    is ResultadoEsteganografia.Error        -> actual.copy(error = r.mensaje, estaCargando = false)
                }
            } catch (ex: OutOfMemoryError) {
                fallar("Imagen demasiado grande para este dispositivo")
            }
        }
    }

    fun guardar(resolver: ContentResolver, uri: Uri) {
        val resultado = _estado.value.imagenResultado ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = Esteganografia.bitmapABytes(resultado)
                resolver.openOutputStream(uri, "wt")?.use { it.write(bytes) }
                    ?: throw java.io.IOException("No se pudo abrir el destino")
                _estado.value = _estado.value.copy(info = "Imagen guardada como PNG. No la recomprimas ni la envíes por servicios que conviertan a JPEG.")
            } catch (e: Exception) {
                fallar("No se pudo guardar: ${e.localizedMessage}")
            }
        }
    }

    private fun fallar(mensaje: String) {
        _estado.value = _estado.value.copy(error = mensaje, estaCargando = false)
    }
}

@Composable
fun PantallaEsteganografia(
    viewModel:      ViewModelEsteganografia = androidx.lifecycle.viewmodel.compose.viewModel(),
    alRetroceder:   () -> Unit,
    alVerEducativo: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current
    val portapapeles = LocalClipboardManager.current

    val elegirImagen = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.cargarImagen(contexto.contentResolver, uri, nombreDeUri(contexto, uri))
    }
    val guardarImagen = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        if (uri != null) viewModel.guardar(contexto.contentResolver, uri)
    }

    Scaffold(
        topBar = { BarraSuperiorCriptES("Esteganografía", "LSB", alRetroceder, alVerEducativo) },
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
                opciones     = listOf("🖼️ Ocultar", "🔍 Extraer"),
                seleccionada = estado.modo,
                alCambiar    = viewModel::cambiarModo
            )

            OutlinedButton(
                onClick  = { elegirImagen.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                enabled  = !estado.estaCargando
            ) {
                Text(
                    if (estado.imagen == null) "Elegir imagen" else "Cambiar imagen (${estado.nombreImagen})",
                    color = RojoVino400
                )
            }

            estado.imagen?.let { imagen ->
                Image(
                    bitmap             = imagen.asImageBitmap(),
                    contentDescription = "Imagen elegida",
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier.fillMaxWidth().heightIn(max = 220.dp).clip(RoundedCornerShape(12.dp))
                )
                if (estado.modo == EstadoEsteganografia.MODO_OCULTAR) {
                    Text(
                        "Capacidad: ~${Esteganografia.calcularCapacidad(imagen)} bytes de mensaje " +
                        "(con contraseña, algo menos). Se guarda como PNG.",
                        style = MaterialTheme.typography.bodySmall, color = GrisClaro
                    )
                }
            }

            if (estado.modo == EstadoEsteganografia.MODO_OCULTAR) {
                CampoTextoEstilizado(
                    valor = estado.mensaje, etiqueta = "Mensaje a ocultar",
                    marcador = "Escribe tu mensaje secreto...", onCambio = viewModel::actualizarMensaje,
                    lineasMaximas = 6
                )
            }

            CampoContrasena(
                valor = estado.contrasena, onCambio = viewModel::actualizarContrasena,
                etiqueta = "Contraseña (opcional)"
            )
            Text(
                if (estado.modo == EstadoEsteganografia.MODO_OCULTAR)
                    "Sin contraseña, cualquiera con una herramienta LSB puede leer el mensaje. Con contraseña se cifra con AES-256-GCM antes de ocultarlo."
                else
                    "Introduce la contraseña solo si el mensaje se ocultó con una.",
                style = MaterialTheme.typography.bodySmall, color = GrisMedio
            )

            BotonPrincipal(
                texto        = if (estado.modo == EstadoEsteganografia.MODO_OCULTAR) "🖼️ Ocultar mensaje" else "🔍 Extraer mensaje",
                estaCargando = estado.estaCargando,
                alClick      = viewModel::ejecutar
            )

            estado.error?.let { TarjetaError(mensaje = it) }
            estado.info?.let { TarjetaInfo(mensaje = it) }

            estado.imagenResultado?.let { resultado ->
                Image(
                    bitmap             = resultado.asImageBitmap(),
                    contentDescription = "Imagen con mensaje oculto",
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier.fillMaxWidth().heightIn(max = 220.dp).clip(RoundedCornerShape(12.dp))
                )
                BotonPrincipal(
                    texto        = "💾 Guardar PNG",
                    estaCargando = false,
                    alClick      = { guardarImagen.launch("criptes_oculto.png") }
                )
            }

            if (estado.mensajeExtraido.isNotEmpty()) {
                TarjetaResultado(
                    resultado = estado.mensajeExtraido,
                    esExito   = true,
                    alCopiar  = { portapapeles.setText(AnnotatedString(estado.mensajeExtraido)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
