package com.criptes.app.ui.pantallas

// ============================================================
//  CriptES — Stubs de pantallas pendientes de implementación
//  PantallasCifradoRSA, PantallaEsteganografia, PantallaCifradoArchivo
// ============================================================

// Estas pantallas están definidas aquí como stubs para que el
// proyecto compile correctamente. Su implementación completa
// sigue la misma estructura que PantallaCifradoTexto.kt

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.criptes.app.ui.tema.*

// ── Pantalla RSA ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCifradoRSA(
    alRetroceder:   () -> Unit,
    alVerEducativo: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cifrado RSA", color = BlancoPuro) },
                navigationIcon = {
                    IconButton(onClick = alRetroceder) {
                        Icon(Icons.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                    }
                },
                actions = {
                    IconButton(onClick = { alVerEducativo("RSA") }) {
                        Icon(Icons.Filled.School, "Aprender", tint = RojoVino400)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NegroPuro)
            )
        },
        containerColor = NegroPuro
    ) { padding ->
        PantallaEnConstruccion(
            icono    = "🔑",
            titulo   = "Cifrado RSA",
            subtitulo = "Generación de claves pública/privada y cifrado asimétrico",
            modifier = Modifier.padding(padding)
        )
    }
}

// ── Pantalla Esteganografía ──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEsteganografia(
    alRetroceder:   () -> Unit,
    alVerEducativo: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Esteganografía", color = BlancoPuro) },
                navigationIcon = {
                    IconButton(onClick = alRetroceder) {
                        Icon(Icons.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                    }
                },
                actions = {
                    IconButton(onClick = { alVerEducativo("LSB") }) {
                        Icon(Icons.Filled.School, "Aprender", tint = RojoVino400)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NegroPuro)
            )
        },
        containerColor = NegroPuro
    ) { padding ->
        PantallaEnConstruccion(
            icono    = "🖼️",
            titulo   = "Esteganografía LSB",
            subtitulo = "Oculta mensajes secretos dentro de imágenes PNG",
            modifier = Modifier.padding(padding)
        )
    }
}

// ── Pantalla Cifrado de Archivos ─────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCifradoArchivo(
    alRetroceder:   () -> Unit,
    alVerEducativo: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cifrado de Archivos", color = BlancoPuro) },
                navigationIcon = {
                    IconButton(onClick = alRetroceder) {
                        Icon(Icons.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                    }
                },
                actions = {
                    IconButton(onClick = { alVerEducativo("AES") }) {
                        Icon(Icons.Filled.School, "Aprender", tint = RojoVino400)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NegroPuro)
            )
        },
        containerColor = NegroPuro
    ) { padding ->
        PantallaEnConstruccion(
            icono    = "📁",
            titulo   = "Cifrado de Archivos",
            subtitulo = "Protege archivos completos con AES-256",
            modifier = Modifier.padding(padding)
        )
    }
}

// ── Componente "En Construcción" ─────────────────────────────

@Composable
private fun PantallaEnConstruccion(
    icono:    String,
    titulo:   String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = icono, style = MaterialTheme.typography.displayMedium)
            Text(
                text  = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = BlancoPuro
            )
            Text(
                text  = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = GrisClaro
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = RojoVino900.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, RojoVino700.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text     = "🚧  Próximamente en v1.1",
                    style    = MaterialTheme.typography.labelMedium,
                    color    = RojoVino400,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
