package com.criptes.app.ui.tema

import androidx.compose.ui.graphics.Color

// ============================================================
//  CriptES — Paleta de colores
//  Identidad visual: Rojo vino + Negro puro
// ============================================================

// ── Rojos Vino (Color primario) ──────────────────────────────
val RojoVino900   = Color(0xFF3B0010)   // Más oscuro
val RojoVino800   = Color(0xFF5C0A1E)   // Oscuro profundo
val RojoVino700   = Color(0xFF7B1A2E)   // Rojo vino principal ★
val RojoVino600   = Color(0xFF9A2340)   // Medio
val RojoVino500   = Color(0xFFB22948)  // Claro/Acento
val RojoVino400   = Color(0xFFC94E6A)   // Suave
val RojoVino300   = Color(0xFFDA7A90)   // Muy suave
val RojoVino200   = Color(0xFFEAA8B5)   // Pastel
val RojoVino100   = Color(0xFFF5D5DC)   // Casi blanco rosado

// ── Negros y Grises (Fondo) ──────────────────────────────────
val NegroPuro      = Color(0xFF000000)   // Fondo principal ★
val NegroProf      = Color(0xFF0A0A0A)   // Fondo secundario
val NegroCard      = Color(0xFF111111)   // Superficie de tarjetas
val NegroElevado   = Color(0xFF1A0A0E)   // Cards con tinte vino
val GrisOscuro     = Color(0xFF2A2A2A)   // Bordes, separadores
val GrisMedio      = Color(0xFF6B6B6B)   // Texto deshabilitado
val GrisClaro      = Color(0xFF9E9E9E)   // Texto secundario
val GrisSuave      = Color(0xFFD0D0D0)   // Texto terciario

// ── Blancos (Texto) ──────────────────────────────────────────
val BlancoPuro     = Color(0xFFFFFFFF)   // Texto principal ★
val BlancoSuave    = Color(0xFFF5F5F5)   // Texto sobre fondo negro

// ── Colores semánticos ───────────────────────────────────────
val VerdeExito    = Color(0xFF2E7D32)    // Operación exitosa
val VerdeExitoClaro = Color(0xFF4CAF50) // Ícono de éxito
val AmarilloAviso = Color(0xFFF9A825)   // Advertencia
val RojoError     = Color(0xFFB71C1C)   // Error crítico
val AzulInfo      = Color(0xFF1565C0)   // Información

// ── Gradientes predefinidos ──────────────────────────────────
val GradientePrincipal = listOf(NegroPuro, RojoVino900)
val GradienteCard      = listOf(NegroElevado, NegroCard)
val GradienteBoton     = listOf(RojoVino700, RojoVino800)
