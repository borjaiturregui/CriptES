package com.criptes.app.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.criptes.app.ui.pantallas.*

// ============================================================
//  CriptES — Sistema de navegación
//  Define todas las rutas y el grafo de navegación
// ============================================================

/**
 * Objeto que centraliza todas las rutas de la aplicación.
 * Usar constantes evita errores tipográficos en la navegación.
 */
object Rutas {
    const val INICIO          = "inicio"
    const val CIFRADO_TEXTO   = "cifrado_texto"
    const val CIFRADO_RSA     = "cifrado_rsa"
    const val GENERADOR_HASH  = "generador_hash"
    const val ESTEGANOGRAFIA  = "esteganografia"
    const val CIFRADO_ARCHIVO = "cifrado_archivo"
    const val EDUCATIVO       = "educativo/{algoritmo}"

    // Función de utilidad para construir la ruta educativa con parámetro
    fun educativo(algoritmo: String) = "educativo/$algoritmo"
}

/**
 * Elemento del menú de navegación inferior.
 */
data class ElementoNavegacion(
    val ruta: String,
    val titulo: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector,
    val iconoSeleccionado: androidx.compose.ui.graphics.vector.ImageVector = icono
)

/**
 * Grafo principal de navegación de CriptES.
 * Conecta todas las pantallas de la aplicación.
 *
 * @param controladorNav Controlador de navegación de Compose
 */
@Composable
fun GrafoNavegacion(
    controladorNav: NavHostController = rememberNavController()
) {
    NavHost(
        navController    = controladorNav,
        startDestination = Rutas.INICIO
    ) {

        // ── Pantalla de inicio ───────────────────────────────
        composable(Rutas.INICIO) {
            PantallaInicio(
                alNavegar = { ruta -> controladorNav.navigate(ruta) }
            )
        }

        // ── Cifrado simétrico de texto ───────────────────────
        composable(Rutas.CIFRADO_TEXTO) {
            PantallaCifradoTexto(
                alRetroceder    = { controladorNav.popBackStack() },
                alVerEducativo  = { algo -> controladorNav.navigate(Rutas.educativo(algo)) }
            )
        }

        // ── Cifrado asimétrico RSA ───────────────────────────
        composable(Rutas.CIFRADO_RSA) {
            PantallaCifradoRSA(
                alRetroceder   = { controladorNav.popBackStack() },
                alVerEducativo = { algo -> controladorNav.navigate(Rutas.educativo(algo)) }
            )
        }

        // ── Generador de hashes ──────────────────────────────
        composable(Rutas.GENERADOR_HASH) {
            PantallaGeneradorHash(
                alRetroceder   = { controladorNav.popBackStack() },
                alVerEducativo = { algo -> controladorNav.navigate(Rutas.educativo(algo)) }
            )
        }

        // ── Esteganografía en imágenes ───────────────────────
        composable(Rutas.ESTEGANOGRAFIA) {
            PantallaEsteganografia(
                alRetroceder   = { controladorNav.popBackStack() },
                alVerEducativo = { algo -> controladorNav.navigate(Rutas.educativo(algo)) }
            )
        }

        // ── Cifrado de archivos ──────────────────────────────
        composable(Rutas.CIFRADO_ARCHIVO) {
            PantallaCifradoArchivo(
                alRetroceder   = { controladorNav.popBackStack() },
                alVerEducativo = { algo -> controladorNav.navigate(Rutas.educativo(algo)) }
            )
        }

        // ── Pantalla educativa (con parámetro de algoritmo) ──
        composable(Rutas.EDUCATIVO) { entrada ->
            val algoritmo = entrada.arguments?.getString("algoritmo") ?: "AES"
            PantallaEducativa(
                algoritmo    = algoritmo,
                alRetroceder = { controladorNav.popBackStack() }
            )
        }
    }
}
