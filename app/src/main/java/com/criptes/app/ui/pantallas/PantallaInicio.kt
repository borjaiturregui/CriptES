package com.criptes.app.ui.pantallas

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.criptes.app.ui.navegacion.Rutas
import com.criptes.app.ui.tema.*

// ============================================================
//  CriptES — Pantalla de Inicio
//  Menú principal con acceso a todos los módulos
// ============================================================

/**
 * Modelo que representa cada módulo/herramienta de la app.
 */
data class ModuloCriptes(
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val ruta: String,
    val etiqueta: String = ""      // Ej: "NUEVO", "RSA"
)

/**
 * Lista de todos los módulos disponibles en CriptES.
 */
private val modulos = listOf(
    ModuloCriptes(
        titulo      = "Cifrado de Texto",
        descripcion = "AES, DES, 3DES y ChaCha20 para cifrar mensajes",
        icono       = Icons.Filled.Lock,
        ruta        = Rutas.CIFRADO_TEXTO
    ),
    ModuloCriptes(
        titulo      = "Cifrado RSA",
        descripcion = "Cifrado asimétrico con clave pública y privada",
        icono       = Icons.Filled.Key,
        ruta        = Rutas.CIFRADO_RSA,
        etiqueta    = "RSA"
    ),
    ModuloCriptes(
        titulo      = "Generador de Hashes",
        descripcion = "MD5, SHA-1, SHA-256 y SHA-512",
        icono       = Icons.Filled.Tag,
        ruta        = Rutas.GENERADOR_HASH
    ),
    ModuloCriptes(
        titulo      = "Esteganografía",
        descripcion = "Oculta mensajes secretos dentro de imágenes",
        icono       = Icons.Filled.HideImage,
        ruta        = Rutas.ESTEGANOGRAFIA
    ),
    ModuloCriptes(
        titulo      = "Cifrado de Archivos",
        descripcion = "Protege archivos completos con AES-256",
        icono       = Icons.Filled.FolderOff,
        ruta        = Rutas.CIFRADO_ARCHIVO
    )
)

/**
 * Pantalla principal de CriptES.
 * Muestra la cabecera de la app y la cuadrícula de módulos.
 *
 * @param alNavegar Lambda que recibe la ruta de destino
 */
@Composable
fun PantallaInicio(
    alNavegar: (String) -> Unit
) {
    // Estado para la animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NegroPuro)
    ) {
        // Decoración: línea vino vertical en el borde izquierdo
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, RojoVino700, Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // ── Cabecera animada ─────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn() + slideInVertically(initialOffsetY = { -40 })
            ) {
                CabeceraCriptES()
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Cuadrícula de módulos ─────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = 200))
            ) {
                CuadriculaModulos(
                    modulos    = modulos,
                    alNavegar  = alNavegar
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Pie de página ─────────────────────────────────
            PiePagina()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Cabecera con logo y descripción de CriptES.
 */
@Composable
private fun CabeceraCriptES() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ícono de la app
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(RojoVino700, RojoVino900)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.Security,
                    contentDescription = null,
                    tint               = BlancoPuro,
                    modifier           = Modifier.size(32.dp)
                )
            }

            Column {
                Text(
                    text       = "CriptES",
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color      = BlancoPuro
                )
                Text(
                    text  = "Kit de Seguridad Criptográfica",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RojoVino400
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banner informativo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NegroElevado)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(RojoVino900, Color.Transparent)),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Filled.WifiOff,
                contentDescription = null,
                tint               = RojoVino400,
                modifier           = Modifier.size(16.dp)
            )
            Text(
                text  = "100% offline — Sin internet, sin recopilación de datos",
                style = MaterialTheme.typography.labelSmall,
                color = GrisClaro
            )
        }
    }
}

/**
 * Cuadrícula adaptable de módulos de la aplicación.
 */
@Composable
private fun CuadriculaModulos(
    modulos:   List<ModuloCriptes>,
    alNavegar: (String) -> Unit
) {
    // Calculamos manualmente el grid ya que estamos dentro de un scroll column
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text       = "Herramientas",
            style      = MaterialTheme.typography.titleMedium,
            color      = GrisClaro,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(bottom = 4.dp)
        )

        // Fila 1: primeros 2 módulos
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            modulos.take(2).forEach { modulo ->
                TarjetaModulo(
                    modulo    = modulo,
                    alNavegar = alNavegar,
                    modifier  = Modifier.weight(1f)
                )
            }
        }

        // Fila 2: siguiente 2 módulos
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            modulos.drop(2).take(2).forEach { modulo ->
                TarjetaModulo(
                    modulo    = modulo,
                    alNavegar = alNavegar,
                    modifier  = Modifier.weight(1f)
                )
            }
            // Si hay módulo impar, también añadimos uno
            if (modulos.drop(2).size < 2) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Módulo central restante (ancho completo si está solo)
        if (modulos.size % 2 != 0) {
            TarjetaModuloAncho(
                modulo    = modulos.last(),
                alNavegar = alNavegar
            )
        }
    }
}

/**
 * Tarjeta individual de módulo (formato cuadrado).
 */
@Composable
private fun TarjetaModulo(
    modulo:    ModuloCriptes,
    alNavegar: (String) -> Unit,
    modifier:  Modifier = Modifier
) {
    var presionado by remember { mutableStateOf(false) }
    val escala by animateFloatAsState(
        targetValue    = if (presionado) 0.96f else 1f,
        animationSpec  = spring(stiffness = Spring.StiffnessMediumLow),
        label          = "escala_tarjeta"
    )

    Card(
        onClick = { alNavegar(modulo.ruta) },
        modifier = modifier
            .aspectRatio(0.9f)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NegroCard),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(RojoVino900.copy(alpha = 0.6f), Color.Transparent)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente decorativo en la esquina superior
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = (-10).dp, y = (-10).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(RojoVino900.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Ícono y etiqueta
                Column {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(RojoVino900),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = modulo.icono,
                            contentDescription = null,
                            tint               = RojoVino400,
                            modifier           = Modifier.size(26.dp)
                        )
                    }

                    if (modulo.etiqueta.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RojoVino700.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text     = modulo.etiqueta,
                                style    = MaterialTheme.typography.labelSmall,
                                color    = RojoVino300,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Texto
                Column {
                    Text(
                        text       = modulo.titulo,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = BlancoPuro,
                        maxLines   = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text     = modulo.descripcion,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = GrisClaro,
                        maxLines = 3
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de módulo en formato ancho (ocupa toda la fila).
 */
@Composable
private fun TarjetaModuloAncho(
    modulo:    ModuloCriptes,
    alNavegar: (String) -> Unit
) {
    Card(
        onClick = { alNavegar(modulo.ruta) },
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = NegroElevado),
        border   = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(RojoVino900.copy(alpha = 0.8f), Color.Transparent)
            )
        )
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RojoVino900),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = modulo.icono,
                    contentDescription = null,
                    tint               = RojoVino400,
                    modifier           = Modifier.size(30.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = modulo.titulo,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = BlancoPuro
                )
                Text(
                    text  = modulo.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrisClaro
                )
            }

            Icon(
                imageVector        = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint               = RojoVino600
            )
        }
    }
}

/**
 * Pie de página informativo.
 */
@Composable
private fun PiePagina() {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            color     = GrisOscuro,
            thickness = 1.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text      = "CriptES v1.0.0 — Proyecto educativo de código abierto",
            style     = MaterialTheme.typography.labelSmall,
            color     = GrisMedio,
            textAlign = TextAlign.Center
        )
        Text(
            text      = "Sin internet · Sin datos · Sin anuncios",
            style     = MaterialTheme.typography.labelSmall,
            color     = RojoVino700,
            textAlign = TextAlign.Center
        )
    }
}
