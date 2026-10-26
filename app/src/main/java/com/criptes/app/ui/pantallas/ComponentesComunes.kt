package com.criptes.app.ui.pantallas

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.criptes.app.ui.tema.*

// ============================================================
//  CriptES — Componentes compartidos por las pantallas de módulos
// ============================================================

/** Barra superior con botón de volver y acceso al modo educativo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperiorCriptES(
    titulo:         String,
    claveEducativa: String,
    alRetroceder:   () -> Unit,
    alVerEducativo: (String) -> Unit
) {
    TopAppBar(
        title = { Text(titulo, color = BlancoPuro) },
        navigationIcon = {
            IconButton(onClick = alRetroceder) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = BlancoPuro)
            }
        },
        actions = {
            IconButton(onClick = { alVerEducativo(claveEducativa) }) {
                Icon(Icons.Filled.School, contentDescription = "Aprender más", tint = RojoVino400)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NegroPuro)
    )
}

/** Selector de varias opciones (generalización de Cifrar/Descifrar). */
@Composable
fun SelectorSegmentado(
    opciones:     List<String>,
    seleccionada: Int,
    alCambiar:    (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NegroCard)
            .padding(4.dp)
    ) {
        opciones.forEachIndexed { indice, etiqueta ->
            val activa = indice == seleccionada
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activa) RojoVino700 else Color.Transparent)
                    .clickable { alCambiar(indice) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = etiqueta,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = if (activa) FontWeight.Bold else FontWeight.Normal,
                    color      = if (activa) BlancoPuro else GrisClaro
                )
            }
        }
    }
}

/** Aviso amarillo (advertencias de seguridad). */
@Composable
fun TarjetaAviso(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = AmarilloAviso.copy(alpha = 0.08f)),
        border   = BorderStroke(1.dp, AmarilloAviso.copy(alpha = 0.4f))
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = AmarilloAviso, modifier = Modifier.size(20.dp))
            Text(mensaje, style = MaterialTheme.typography.bodySmall, color = AmarilloAviso)
        }
    }
}

/** Mensaje informativo/éxito (p. ej. "Archivo guardado"). */
@Composable
fun TarjetaInfo(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = VerdeExito.copy(alpha = 0.12f)),
        border   = BorderStroke(1.dp, VerdeExito.copy(alpha = 0.5f))
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeExitoClaro, modifier = Modifier.size(20.dp))
            Text(mensaje, style = MaterialTheme.typography.bodySmall, color = BlancoSuave)
        }
    }
}

/** Tarjeta con título, texto seleccionable y botón de copiar. */
@Composable
fun TarjetaTextoCopiable(titulo: String, texto: String, alCopiar: () -> Unit) {
    var copiado by remember(texto) { mutableStateOf(false) }
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
                Text(
                    text       = titulo,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = BlancoPuro
                )
                IconButton(onClick = { alCopiar(); copiado = true }) {
                    Icon(
                        if (copiado) Icons.Filled.Check else Icons.Filled.ContentCopy,
                        contentDescription = "Copiar",
                        tint = if (copiado) VerdeExitoClaro else RojoVino400
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.text.selection.SelectionContainer {
                Text(text = texto, style = MaterialTheme.typography.bodySmall, color = GrisClaro)
            }
        }
    }
}

/** Fila con interruptor y etiqueta. */
@Composable
fun InterruptorConEtiqueta(etiqueta: String, activo: Boolean, alCambiar: (Boolean) -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = BlancoSuave)
        Switch(
            checked         = activo,
            onCheckedChange = alCambiar,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = BlancoPuro,
                checkedTrackColor   = RojoVino700,
                uncheckedThumbColor = GrisClaro,
                uncheckedTrackColor = GrisOscuro
            )
        )
    }
}

/** Nombre visible de un documento elegido con el selector del sistema. */
fun nombreDeUri(contexto: Context, uri: Uri): String? = try {
    contexto.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
} catch (e: Exception) {
    null
}
