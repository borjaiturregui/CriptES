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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.criptes.app.dominio.modelos.*
import com.criptes.app.ui.tema.*

// ============================================================
//  CriptES — Pantalla Educativa
//  Explica cada algoritmo criptográfico de forma didáctica
// ============================================================

/**
 * Pantalla del Modo Educativo de CriptES.
 * Muestra información completa y didáctica sobre un algoritmo.
 *
 * @param algoritmo  Nombre del algoritmo a explicar (ej: "AES", "RSA")
 * @param alRetroceder Lambda para volver a la pantalla anterior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEducativa(
    algoritmo:   String,
    alRetroceder: () -> Unit
) {
    val info = ContenidoEducativo.obtener(algoritmo)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.School,
                            contentDescription = null,
                            tint               = RojoVino400,
                            modifier           = Modifier.size(20.dp)
                        )
                        Text(
                            text  = "Modo Educativo",
                            color = BlancoPuro
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alRetroceder) {
                        Icon(
                            imageVector        = Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint               = BlancoPuro
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NegroPuro
                )
            )
        },
        containerColor = NegroPuro
    ) { padding ->

        if (info == null) {
            // Algoritmo no encontrado
            Box(
                modifier            = Modifier.fillMaxSize().padding(padding),
                contentAlignment    = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector        = Icons.Filled.FindInPage,
                        contentDescription = null,
                        tint               = RojoVino600,
                        modifier           = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text  = "Contenido no disponible",
                        style = MaterialTheme.typography.titleMedium,
                        color = GrisClaro
                    )
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Encabezado del algoritmo ─────────────────────
            EncabezadoAlgoritmo(info)

            // ── Historia ─────────────────────────────────────
            SeccionEducativa(
                titulo    = "📖 Historia",
                icono     = Icons.Filled.AutoStories,
                contenido = info.historia
            )

            // ── Cómo funciona ─────────────────────────────────
            SeccionEducativa(
                titulo    = "⚙️ ¿Cómo funciona?",
                icono     = Icons.Filled.Settings,
                contenido = info.comoFunciona
            )

            // ── Fortalezas y debilidades ──────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fortalezas
                TarjetaLista(
                    titulo   = "✅ Fortalezas",
                    items    = info.fortalezas,
                    colorBorde = VerdeExito,
                    modifier = Modifier.weight(1f)
                )
                // Debilidades
                TarjetaLista(
                    titulo     = "⚠️ Debilidades",
                    items      = info.debilidades,
                    colorBorde = AmarilloAviso,
                    modifier   = Modifier.weight(1f)
                )
            }

            // ── Casos de uso ──────────────────────────────────
            TarjetaLista(
                titulo     = "🌐 Casos de Uso Reales",
                items      = info.casosDeUso,
                colorBorde = RojoVino700,
                modifier   = Modifier.fillMaxWidth()
            )

            // ── Dato curioso ──────────────────────────────────
            TarjetaDatoCurioso(texto = info.datoCurioso)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Encabezado con nombre del algoritmo y nivel de seguridad.
 */
@Composable
private fun EncabezadoAlgoritmo(info: InfoEducativa) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = NegroElevado),
        border   = BorderStroke(1.dp, RojoVino900)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Categoría
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RojoVino900.copy(alpha = 0.6f)
            ) {
                Text(
                    text     = info.categoria.etiqueta,
                    style    = MaterialTheme.typography.labelMedium,
                    color    = RojoVino300,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre
            Text(
                text       = info.nombre,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = BlancoPuro
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metadatos
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetaDato(icono = Icons.Filled.CalendarToday, valor = info.añoCreacion.toString())
                MetaDato(icono = Icons.Filled.Person, valor = info.creadores.split(",").first().trim())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nivel de seguridad
            val (colorNivel, iconoNivel) = when (info.nivelSeguridad) {
                NivelSeguridad.ROTO       -> Pair(RojoError,     Icons.Filled.Dangerous)
                NivelSeguridad.DEBIL      -> Pair(AmarilloAviso, Icons.Filled.Warning)
                NivelSeguridad.MODERADO   -> Pair(Color(0xFFFFA726), Icons.Filled.Shield)
                NivelSeguridad.FUERTE     -> Pair(VerdeExitoClaro, Icons.Filled.VerifiedUser)
                NivelSeguridad.MUY_FUERTE -> Pair(VerdeExitoClaro, Icons.Filled.Security)
            }

            Row(
                modifier            = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorNivel.copy(alpha = 0.1f))
                    .padding(12.dp),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = iconoNivel,
                    contentDescription = null,
                    tint               = colorNivel,
                    modifier           = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text       = "Nivel de Seguridad: ${info.nivelSeguridad.etiqueta}",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = colorNivel
                    )
                    Text(
                        text  = info.nivelSeguridad.descripcion,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorNivel.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Sección con título e ícono para historia y funcionamiento.
 */
@Composable
private fun SeccionEducativa(
    titulo:    String,
    icono:     androidx.compose.ui.graphics.vector.ImageVector,
    contenido: String
) {
    var expandido by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = NegroCard),
        border   = BorderStroke(1.dp, GrisOscuro)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado clickeable para expandir/colapsar
            Row(
                modifier      = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector        = icono,
                        contentDescription = null,
                        tint               = RojoVino400,
                        modifier           = Modifier.size(20.dp)
                    )
                    Text(
                        text       = titulo,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = BlancoPuro
                    )
                }
                Icon(
                    imageVector        = if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expandido) "Colapsar" else "Expandir",
                    tint               = GrisClaro
                )
            }

            // Contenido animado
            AnimatedVisibility(
                visible = expandido,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = GrisOscuro)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text  = contenido,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrisClaro,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de lista para fortalezas, debilidades y casos de uso.
 */
@Composable
private fun TarjetaLista(
    titulo:     String,
    items:      List<String>,
    colorBorde: Color,
    modifier:   Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = NegroCard),
        border   = BorderStroke(1.dp, colorBorde.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text       = titulo,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = BlancoPuro
            )
            Spacer(modifier = Modifier.height(10.dp))
            items.forEach { item ->
                Row(
                    modifier              = Modifier.padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .offset(y = 6.dp)
                            .background(colorBorde, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text  = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisClaro
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta especial para el dato curioso.
 */
@Composable
private fun TarjetaDatoCurioso(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = RojoVino900.copy(alpha = 0.3f)),
        border   = BorderStroke(1.dp, RojoVino700.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "💡", style = MaterialTheme.typography.titleMedium)
                Text(
                    text       = "¿Sabías que...?",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = RojoVino300
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text  = texto,
                style = MaterialTheme.typography.bodySmall,
                color = GrisClaro
            )
        }
    }
}

/**
 * Pequeño chip de metadato con ícono y valor.
 */
@Composable
private fun MetaDato(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector        = icono,
            contentDescription = null,
            tint               = GrisMedio,
            modifier           = Modifier.size(14.dp)
        )
        Text(
            text  = valor,
            style = MaterialTheme.typography.labelSmall,
            color = GrisMedio
        )
    }
}
