package com.criptes.app.ui.pantallas

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criptes.app.criptografia.*
import com.criptes.app.ui.tema.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ============================================================
//  CriptES — Pantalla de Cifrado Simétrico de Texto
//  AES-256, DES, 3DES, ChaCha20
// ============================================================

// ── ViewModel ────────────────────────────────────────────────

/**
 * Estado de la UI para la pantalla de cifrado de texto.
 */
data class EstadoCifradoTexto(
    val textoEntrada:   String             = "",
    val contrasena:     String             = "",
    val algoritmo:      AlgoritmoSimetrico = AlgoritmoSimetrico.AES,
    val resultado:      String             = "",
    val error:          String?            = null,
    val estaCargando:   Boolean            = false,
    val modoCifrar:     Boolean            = true   // true = cifrar, false = descifrar
)

/**
 * ViewModel que gestiona la lógica de cifrado simétrico.
 * Separa la lógica de negocio de la UI (patrón MVVM).
 */
class ViewModelCifradoTexto : ViewModel() {

    private val _estado = MutableStateFlow(EstadoCifradoTexto())
    val estado: StateFlow<EstadoCifradoTexto> = _estado.asStateFlow()

    fun actualizarTexto(texto: String)      { _estado.value = _estado.value.copy(textoEntrada = texto, resultado = "", error = null) }
    fun actualizarContrasena(pass: String)  { _estado.value = _estado.value.copy(contrasena = pass, error = null) }
    fun actualizarAlgoritmo(algo: AlgoritmoSimetrico) { _estado.value = _estado.value.copy(algoritmo = algo, resultado = "", error = null) }
    fun cambiarModo(cifrar: Boolean)        { _estado.value = _estado.value.copy(modoCifrar = cifrar, textoEntrada = "", resultado = "", error = null) }
    fun limpiar() { _estado.value = EstadoCifradoTexto() }

    /**
     * Ejecuta la operación de cifrado o descifrado en un hilo de fondo.
     */
    fun ejecutar() {
        val estado = _estado.value
        if (estado.textoEntrada.isBlank() || estado.contrasena.isBlank()) {
            _estado.value = _estado.value.copy(error = "Completa todos los campos")
            return
        }

        _estado.value = _estado.value.copy(estaCargando = true, error = null)

        viewModelScope.launch(Dispatchers.IO) {
            val resultado = if (estado.modoCifrar) {
                CifradoSimetrico.cifrar(estado.textoEntrada, estado.contrasena, estado.algoritmo)
            } else {
                CifradoSimetrico.descifrar(estado.textoEntrada, estado.contrasena, estado.algoritmo)
            }

            when (resultado) {
                is ResultadoCifrado.Exito ->
                    _estado.value = _estado.value.copy(
                        resultado    = resultado.texto,
                        estaCargando = false
                    )
                is ResultadoCifrado.Error ->
                    _estado.value = _estado.value.copy(
                        error        = resultado.mensaje,
                        estaCargando = false
                    )
            }
        }
    }
}

// ── UI ───────────────────────────────────────────────────────

/**
 * Pantalla de Cifrado Simétrico de Texto.
 * Permite cifrar y descifrar texto con AES, DES, 3DES y ChaCha20.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCifradoTexto(
    viewModel:     ViewModelCifradoTexto = androidx.lifecycle.viewmodel.compose.viewModel(),
    alRetroceder:  () -> Unit,
    alVerEducativo: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val portapapeles = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Cifrado de Texto",
                        color = BlancoPuro
                    )
                },
                navigationIcon = {
                    IconButton(onClick = alRetroceder) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = BlancoPuro)
                    }
                },
                actions = {
                    // Botón de ayuda educativa
                    IconButton(
                        onClick = { alVerEducativo(estado.algoritmo.name) }
                    ) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = "Aprender más",
                            tint = RojoVino400
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NegroPuro)
            )
        },
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

            // ── Selector de modo (Cifrar / Descifrar) ────────
            SelectorModo(
                modoCifrar    = estado.modoCifrar,
                alCambiarModo = viewModel::cambiarModo
            )

            // ── Selector de algoritmo ─────────────────────────
            SelectorAlgoritmo(
                algoritmoActual     = estado.algoritmo,
                alCambiarAlgoritmo  = viewModel::actualizarAlgoritmo,
                alVerEducativo      = alVerEducativo
            )

            // ── Campo de texto de entrada ─────────────────────
            CampoTextoEstilizado(
                valor              = estado.textoEntrada,
                etiqueta           = if (estado.modoCifrar) "Texto a cifrar" else "Texto cifrado (Base64)",
                marcador           = if (estado.modoCifrar) "Escribe tu mensaje secreto..." else "Pega el texto cifrado aquí...",
                onCambio           = viewModel::actualizarTexto,
                lineasMaximas      = 6
            )

            // ── Campo de contraseña ───────────────────────────
            CampoContrasena(
                valor    = estado.contrasena,
                onCambio = viewModel::actualizarContrasena
            )

            // ── Botón principal ───────────────────────────────
            BotonPrincipal(
                texto       = if (estado.modoCifrar) "🔒 Cifrar" else "🔓 Descifrar",
                estaCargando = estado.estaCargando,
                alClick      = viewModel::ejecutar
            )

            // ── Mensaje de error ──────────────────────────────
            estado.error?.let { error ->
                TarjetaError(mensaje = error)
            }

            // ── Resultado ─────────────────────────────────────
            if (estado.resultado.isNotEmpty()) {
                TarjetaResultado(
                    resultado = estado.resultado,
                    esExito   = true,
                    alCopiar  = {
                        portapapeles.setText(AnnotatedString(estado.resultado))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Componentes reutilizables ─────────────────────────────────

@Composable
private fun SelectorModo(modoCifrar: Boolean, alCambiarModo: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NegroCard)
            .padding(4.dp)
    ) {
        listOf(
            Pair(true,  "🔒 Cifrar"),
            Pair(false, "🔓 Descifrar")
        ).forEach { (esCifrar, etiqueta) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (modoCifrar == esCifrar) RojoVino700
                        else Color.Transparent
                    )
                    .clickable { alCambiarModo(esCifrar) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = etiqueta,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = if (modoCifrar == esCifrar) FontWeight.Bold else FontWeight.Normal,
                    color      = if (modoCifrar == esCifrar) BlancoPuro else GrisClaro
                )
            }
        }
    }
}

@Composable
private fun SelectorAlgoritmo(
    algoritmoActual:    AlgoritmoSimetrico,
    alCambiarAlgoritmo: (AlgoritmoSimetrico) -> Unit,
    alVerEducativo:     (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text  = "Algoritmo",
            style = MaterialTheme.typography.labelLarge,
            color = GrisClaro
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlgoritmoSimetrico.values().forEach { algoritmo ->
                val seleccionado = algoritmo == algoritmoActual
                Surface(
                    onClick  = { alCambiarAlgoritmo(algoritmo) },
                    shape    = RoundedCornerShape(10.dp),
                    color    = if (seleccionado) RojoVino700 else NegroCard,
                    border   = if (seleccionado) null else BorderStroke(1.dp, GrisOscuro),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier            = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text       = algoritmo.nombreMostrar,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                            color      = if (seleccionado) BlancoPuro else GrisClaro
                        )
                    }
                }
            }
        }

        // Info del algoritmo seleccionado
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text  = algoritmoActual.descripcionCorta,
                style = MaterialTheme.typography.bodySmall,
                color = RojoVino400
            )
            TextButton(
                onClick = { alVerEducativo(algoritmoActual.name) }
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = RojoVino600)
                Spacer(modifier = Modifier.width(4.dp))
                Text("¿Qué es?", color = RojoVino600, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun CampoTextoEstilizado(
    valor:          String,
    etiqueta:       String,
    marcador:       String,
    onCambio:       (String) -> Unit,
    lineasMaximas:  Int = 4,
    lineasMinimas:  Int = 3
) {
    OutlinedTextField(
        value         = valor,
        onValueChange = onCambio,
        label         = { Text(etiqueta, color = GrisClaro) },
        placeholder   = { Text(marcador, color = GrisMedio, style = MaterialTheme.typography.bodySmall) },
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(14.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedTextColor         = BlancoPuro,
            unfocusedTextColor       = BlancoPuro,
            focusedContainerColor    = NegroCard,
            unfocusedContainerColor  = NegroCard,
            focusedBorderColor       = RojoVino700,
            unfocusedBorderColor     = GrisOscuro,
            cursorColor              = RojoVino400
        ),
        maxLines      = lineasMaximas,
        minLines      = lineasMinimas
    )
}

@Composable
private fun CampoContrasena(valor: String, onCambio: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value         = valor,
        onValueChange = onCambio,
        label         = { Text("Contraseña", color = GrisClaro) },
        placeholder   = { Text("Tu clave secreta...", color = GrisMedio) },
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(14.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon  = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Ocultar" else "Mostrar",
                    tint = GrisMedio
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor        = BlancoPuro,
            unfocusedTextColor      = BlancoPuro,
            focusedContainerColor   = NegroCard,
            unfocusedContainerColor = NegroCard,
            focusedBorderColor      = RojoVino700,
            unfocusedBorderColor    = GrisOscuro,
            cursorColor             = RojoVino400
        )
    )
}

@Composable
fun BotonPrincipal(texto: String, estaCargando: Boolean, alClick: () -> Unit) {
    Button(
        onClick  = alClick,
        enabled  = !estaCargando,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = RojoVino700,
            disabledContainerColor = RojoVino900
        )
    ) {
        if (estaCargando) {
            CircularProgressIndicator(
                color    = BlancoPuro,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(texto, style = MaterialTheme.typography.labelLarge, color = BlancoPuro)
        }
    }
}

@Composable
fun TarjetaError(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = RojoError.copy(alpha = 0.1f)),
        border   = BorderStroke(1.dp, RojoError.copy(alpha = 0.4f))
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Error, contentDescription = null, tint = RojoError, modifier = Modifier.size(20.dp))
            Text(mensaje, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF6B6B))
        }
    }
}

@Composable
fun TarjetaResultado(resultado: String, esExito: Boolean, alCopiar: () -> Unit) {
    var copiado by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = NegroElevado),
        border   = BorderStroke(1.dp, RojoVino700.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint     = VerdeExitoClaro,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text       = "Resultado",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = BlancoPuro
                    )
                }
                IconButton(
                    onClick = {
                        alCopiar()
                        copiado = true
                    }
                ) {
                    Icon(
                        if (copiado) Icons.Filled.Check else Icons.Filled.ContentCopy,
                        contentDescription = "Copiar",
                        tint = if (copiado) VerdeExitoClaro else RojoVino400
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            SelectionContainer {
                Text(
                    text  = resultado,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrisClaro
                )
            }
        }
    }
}

// Alias necesario para SelectionContainer
@Composable
private fun SelectionContainer(content: @Composable () -> Unit) {
    androidx.compose.foundation.text.selection.SelectionContainer(content = content)
}
