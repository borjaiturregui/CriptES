package com.criptes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.criptes.app.ui.navegacion.GrafoNavegacion
import com.criptes.app.ui.tema.CriptESTema

// ============================================================
//  CriptES — Actividad Principal
//  Punto de entrada de la aplicación Android
// ============================================================

/**
 * Actividad principal de CriptES.
 *
 * Solo se encarga de:
 * 1. Instalar la Splash Screen
 * 2. Configurar Edge-to-Edge (diseño hasta el borde de la pantalla)
 * 3. Inicializar el tema y el grafo de navegación
 *
 * Toda la lógica de negocio vive en los ViewModels y la capa de dominio.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar la splash screen antes del super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Diseño edge-to-edge: el contenido se extiende bajo las barras del sistema
        enableEdgeToEdge()

        setContent {
            CriptESTema {
                // El grafo de navegación gestiona todas las pantallas
                GrafoNavegacion()
            }
        }
    }
}
