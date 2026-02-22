// ============================================================
//  CriptES — build.gradle.kts (RAÍZ) ✅ CORREGIDO v3
//
//  Tabla de compatibilidad usada:
//  ┌─────────────────────┬──────────────┐
//  │ Gradle              │ 8.7          │
//  │ AGP                 │ 8.3.2        │
//  │ Kotlin              │ 1.9.25       │
//  │ Hilt                │ 2.51.1       │
//  └─────────────────────┴──────────────┘
// ============================================================

plugins {
    id("com.android.application")               version "8.3.2"  apply false
    id("com.android.library")                   version "8.3.2"  apply false
    id("org.jetbrains.kotlin.android")          version "1.9.25" apply false
    id("org.jetbrains.kotlin.kapt")             version "1.9.25" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.25" apply false
    id("com.google.dagger.hilt.android")        version "2.51.1" apply false
}