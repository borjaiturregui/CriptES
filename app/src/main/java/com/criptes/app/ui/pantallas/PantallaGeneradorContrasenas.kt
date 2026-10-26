package com.criptes.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.criptes.app.criptografia.*
import com.criptes.app.ui.tema.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

// ============================================================
//  CriptES — Pantalla del Generador de Contraseñas Seguras
// ============================================================

data class EstadoContrasenas(
    val opciones:  OpcionesContrasena = OpcionesContrasena(),
    val resultado: ResultadoContrasena = GeneradorContrasenas.generar(OpcionesContrasena())
)

class ViewModelContrasenas : ViewModel() {

    private val _estado = MutableStateFlow(EstadoContrasenas())
    val estado: StateFlow<EstadoContrasenas> = _estado.asStateFlow()

    /** Cambia las opciones y genera una contraseña nueva con ellas. */
    fun actualizar(opciones: OpcionesContrasena) {
        _estado.value = EstadoContrasenas(opciones, GeneradorContrasenas.generar(opciones))
    }

    fun regenerar() = actualizar(_estado.value.opciones)
}

@Composable
fun PantallaGeneradorContrasenas(
    viewModel:      ViewModelContrasenas = androidx.lifecycle.viewmodel.compose.viewModel(),
    alRetroceder:   () -> Unit,
    alVerEducativo: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val portapapeles = LocalClipboardManager.current
    val o = estado.opciones

    Scaffold(
        topBar = { BarraSuperiorCriptES("Generador de Contraseñas", "CONTRASENAS", alRetroceder, alVerEducativo) },
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

            when (val r = estado.resultado) {
                is ResultadoContrasena.Exito -> {
                    TarjetaTextoCopiable("Contraseña generada", r.contrasena) {
                        portapapeles.setText(AnnotatedString(r.contrasena))
                    }
                    val color = when (r.fuerza) {
                        FuerzaContrasena.DEBIL      -> Color(0xFFFF6B6B)
                        FuerzaContrasena.ACEPTABLE  -> AmarilloAviso
                        FuerzaContrasena.FUERTE     -> VerdeExitoClaro
                        FuerzaContrasena.MUY_FUERTE -> VerdeExitoClaro
                    }
                    Text(
                        "Fuerza: ${r.fuerza.etiqueta} · ${r.entropiaBits.roundToInt()} bits de entropía",
                        style = MaterialTheme.typography.labelLarge, color = color
                    )
                    LinearProgressIndicator(
                        progress   = { (r.entropiaBits / 128.0).coerceIn(0.0, 1.0).toFloat() },
                        modifier   = Modifier.fillMaxWidth(),
                        color      = color,
                        trackColor = GrisOscuro
                    )
                }
                is ResultadoContrasena.Error -> TarjetaError(mensaje = r.mensaje)
            }

            BotonPrincipal("🎲 Generar otra", false, viewModel::regenerar)

            Text(
                "Longitud: ${o.longitud}",
                style = MaterialTheme.typography.labelLarge, color = BlancoSuave
            )
            Slider(
                value         = o.longitud.toFloat(),
                onValueChange = { viewModel.actualizar(o.copy(longitud = it.roundToInt())) },
                valueRange    = GeneradorContrasenas.LONGITUD_MINIMA.toFloat()..GeneradorContrasenas.LONGITUD_MAXIMA.toFloat(),
                colors        = SliderDefaults.colors(
                    thumbColor         = RojoVino500,
                    activeTrackColor   = RojoVino700,
                    inactiveTrackColor = GrisOscuro
                )
            )

            InterruptorConEtiqueta("Minúsculas (a-z)",  o.minusculas)      { viewModel.actualizar(o.copy(minusculas = it)) }
            InterruptorConEtiqueta("Mayúsculas (A-Z)",  o.mayusculas)      { viewModel.actualizar(o.copy(mayusculas = it)) }
            InterruptorConEtiqueta("Números (0-9)",     o.numeros)         { viewModel.actualizar(o.copy(numeros = it)) }
            InterruptorConEtiqueta("Símbolos (!@#…)",   o.simbolos)        { viewModel.actualizar(o.copy(simbolos = it)) }
            InterruptorConEtiqueta("Evitar ambiguos (0 O o 1 l I |)", o.excluirAmbiguos) {
                viewModel.actualizar(o.copy(excluirAmbiguos = it))
            }

            Text(
                "Se genera en tu dispositivo con un generador criptográficamente seguro y no se guarda ni se envía a ningún sitio.",
                style = MaterialTheme.typography.bodySmall, color = GrisMedio
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
