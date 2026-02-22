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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.criptes.app.criptografia.*
import com.criptes.app.ui.tema.*

// ============================================================
//  CriptES — Pantalla Generador de Hashes
//  MD5, SHA-1, SHA-256, SHA-512
// ============================================================

/**
 * Pantalla del Generador de Hashes criptográficos.
 * Muestra los hashes de todos los algoritmos simultáneamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGeneradorHash(
    alRetroceder:   () -> Unit,
    alVerEducativo: (String) -> Unit
) {
    var textoEntrada by remember { mutableStateOf("") }
    val portapapeles = LocalClipboardManager.current

    // Calcular hashes en tiempo real mientras el usuario escribe
    val hashes by remember(textoEntrada) {
        derivedStateOf {
            if (textoEntrada.isBlank()) emptyMap()
            else GeneradorHash.generarTodos(textoEntrada)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generador de Hashes", color = BlancoPuro) },
                navigationIcon = {
                    IconButton(onClick = alRetroceder) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = BlancoPuro)
                    }
                },
                actions = {
                    IconButton(onClick = { alVerEducativo("SHA-256") }) {
                        Icon(Icons.Filled.School, contentDescription = "Aprender", tint = RojoVino400)
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

            // ── Banner informativo ────────────────────────────
            BannerInfo(
                texto = "Escribe cualquier texto y verás su huella digital (hash) en tiempo real con todos los algoritmos."
            )

            // ── Campo de entrada ──────────────────────────────
            CampoTextoEstilizado(
                valor         = textoEntrada,
                etiqueta      = "Texto a hashear",
                marcador      = "Escribe algo... cualquier cosa funciona",
                onCambio      = { textoEntrada = it },
                lineasMaximas = 4,
                lineasMinimas = 2
            )

            // Contador de caracteres
            if (textoEntrada.isNotEmpty()) {
                Text(
                    text  = "${textoEntrada.length} caracteres · ${textoEntrada.toByteArray().size} bytes",
                    style = MaterialTheme.typography.labelSmall,
                    color = GrisMedio,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // ── Resultados de hashes ──────────────────────────
            AnimatedVisibility(
                visible = hashes.isNotEmpty(),
                enter   = fadeIn() + expandVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text  = "Resultados",
                        style = MaterialTheme.typography.titleSmall,
                        color = GrisClaro,
                        fontWeight = FontWeight.SemiBold
                    )

                    AlgoritmoHash.values().forEach { algoritmo ->
                        val hashValor = hashes[algoritmo] ?: ""
                        TarjetaHash(
                            algoritmo  = algoritmo,
                            hash       = hashValor,
                            alCopiar   = {
                                portapapeles.setText(AnnotatedString(hashValor))
                            },
                            alEducativo = { alVerEducativo(algoritmo.name) }
                        )
                    }
                }
            }

            // Estado vacío
            AnimatedVisibility(
                visible = textoEntrada.isBlank(),
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                EstadoVacioHash()
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Tarjeta que muestra un hash con su algoritmo y opciones.
 */
@Composable
private fun TarjetaHash(
    algoritmo:  AlgoritmoHash,
    hash:       String,
    alCopiar:   () -> Unit,
    alEducativo: () -> Unit
) {
    var copiado by remember { mutableStateOf(false) }

    val colorSeguridad = when {
        algoritmo.esSeguro -> VerdeExitoClaro
        else -> AmarilloAviso
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = NegroCard),
        border   = BorderStroke(1.dp, GrisOscuro)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cabecera
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Indicador de seguridad
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(colorSeguridad, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text       = algoritmo.nombreMostrar,
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = BlancoPuro
                    )
                    Text(
                        text  = "${algoritmo.bits} bits",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrisMedio
                    )
                }
                Row {
                    // Botón educativo
                    IconButton(
                        onClick  = alEducativo,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = "Aprender",
                            tint     = RojoVino600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Botón copiar
                    IconButton(
                        onClick  = {
                            alCopiar()
                            copiado = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (copiado) Icons.Filled.Check else Icons.Filled.ContentCopy,
                            contentDescription = "Copiar hash",
                            tint     = if (copiado) VerdeExitoClaro else GrisMedio,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Badge de seguridad
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = colorSeguridad.copy(alpha = 0.1f)
            ) {
                Text(
                    text     = if (algoritmo.esSeguro) "✅ Seguro" else "⚠️ Obsoleto",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = colorSeguridad,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hash en fuente monospace para fácil lectura
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NegroPuro)
                    .padding(10.dp)
            ) {
                androidx.compose.foundation.text.selection.SelectionContainer {
                    Text(
                        text       = hash.chunked(32).joinToString("\n"),  // Dividir en líneas de 32 chars
                        style      = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color      = RojoVino300
                    )
                }
            }

            // Longitud del hash
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text  = "${hash.length} caracteres hexadecimales",
                style = MaterialTheme.typography.labelSmall,
                color = GrisMedio,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Banner informativo con fondo sutil.
 */
@Composable
private fun BannerInfo(texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RojoVino900.copy(alpha = 0.3f))
            .border(1.dp, RojoVino900, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Lightbulb,
            contentDescription = null,
            tint     = RojoVino400,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text  = texto,
            style = MaterialTheme.typography.bodySmall,
            color = GrisClaro
        )
    }
}

/**
 * Vista de estado vacío cuando no hay texto.
 */
@Composable
private fun EstadoVacioHash() {
    Box(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment    = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "#", style = MaterialTheme.typography.displayLarge, color = RojoVino900)
            Text(
                text  = "Escribe algo para ver los hashes",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisMedio
            )
        }
    }
}
