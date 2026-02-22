package com.criptes.app.ui.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
//  CriptES — Tema principal de la aplicación
//  Material Design 3 — Modo oscuro exclusivo
// ============================================================

/**
 * Esquema de colores oscuro de CriptES.
 * Combina rojo vino con negro puro para una identidad visual
 * inconfundible y profesional.
 */
private val EsquemaColorOscuro = darkColorScheme(
    // ── Primarios ───────────────────────────────────────────
    primary          = RojoVino700,        // Color principal de la app
    onPrimary        = BlancoPuro,         // Texto sobre el primario
    primaryContainer = RojoVino900,        // Contenedor con tono primario
    onPrimaryContainer = RojoVino300,      // Texto en contenedor primario

    // ── Secundarios ─────────────────────────────────────────
    secondary        = RojoVino600,        // Acento secundario
    onSecondary      = BlancoPuro,
    secondaryContainer = NegroElevado,     // Contenedor secundario con tinte vino
    onSecondaryContainer = RojoVino200,

    // ── Terciarios ───────────────────────────────────────────
    tertiary         = GrisMedio,
    onTertiary       = BlancoPuro,
    tertiaryContainer = GrisOscuro,
    onTertiaryContainer = GrisClaro,

    // ── Error ────────────────────────────────────────────────
    error            = RojoError,
    onError          = BlancoPuro,
    errorContainer   = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),

    // ── Fondos ───────────────────────────────────────────────
    background       = NegroPuro,          // Fondo principal: negro absoluto
    onBackground     = BlancoPuro,         // Texto sobre el fondo

    // ── Superficies (Cards, Sheets, etc.) ────────────────────
    surface          = NegroCard,          // Superficie de elementos
    onSurface        = BlancoSuave,        // Texto sobre superficie
    surfaceVariant   = NegroElevado,       // Variante con tinte vino
    onSurfaceVariant = GrisClaro,

    // ── Bordes y tintes ──────────────────────────────────────
    outline          = GrisOscuro,         // Bordes de campos de texto
    outlineVariant   = RojoVino900,        // Bordes con tinte vino

    // ── Inversión ────────────────────────────────────────────
    inverseSurface   = BlancoSuave,
    inverseOnSurface = NegroPuro,
    inversePrimary   = RojoVino700,
)

/**
 * Tema principal de CriptES.
 *
 * Uso:
 * ```kotlin
 * CriptESTema {
 *     // Tu contenido Compose aquí
 * }
 * ```
 */
@Composable
fun CriptESTema(
    contenido: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaColorOscuro,
        typography  = Tipografia,
        content     = contenido
    )
}
