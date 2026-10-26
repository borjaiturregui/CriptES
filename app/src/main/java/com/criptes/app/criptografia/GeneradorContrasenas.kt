package com.criptes.app.criptografia

import java.security.SecureRandom
import kotlin.math.ln

// ============================================================
//  CriptES — Generador de contraseñas seguras
// ============================================================

/**
 * Opciones del generador de contraseñas.
 */
data class OpcionesContrasena(
    val longitud:           Int     = 16,
    val minusculas:         Boolean = true,
    val mayusculas:         Boolean = true,
    val numeros:            Boolean = true,
    val simbolos:           Boolean = true,
    val excluirAmbiguos:    Boolean = false   // 0 O o 1 l I |
)

enum class FuerzaContrasena(val etiqueta: String) {
    DEBIL("Débil"), ACEPTABLE("Aceptable"), FUERTE("Fuerte"), MUY_FUERTE("Muy fuerte")
}

sealed class ResultadoContrasena {
    data class Exito(
        val contrasena:  String,
        val entropiaBits: Double,
        val fuerza:      FuerzaContrasena
    ) : ResultadoContrasena()
    data class Error(val mensaje: String) : ResultadoContrasena()
}

/**
 * Genera contraseñas aleatorias con [SecureRandom].
 *
 * - Elige cada carácter de forma uniforme (`nextInt(n)` no tiene sesgo de módulo).
 * - Garantiza al menos un carácter de cada tipo activado y mezcla el resultado
 *   con Fisher-Yates.
 * - La entropía mostrada es `longitud · log2(tamaño del alfabeto)`: es una
 *   cota que sigue siendo válida porque la contraseña se genera al azar. No se
 *   aplica a contraseñas elegidas por una persona.
 */
object GeneradorContrasenas {

    const val LONGITUD_MINIMA = 8
    const val LONGITUD_MAXIMA = 64

    private const val MINUSCULAS = "abcdefghijklmnopqrstuvwxyz"
    private const val MAYUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val NUMEROS    = "0123456789"
    private const val SIMBOLOS   = "!@#$%^&*()-_=+[]{};:,.<>?/~"
    private const val AMBIGUOS   = "0Oo1lI|"

    private val aleatorio = SecureRandom()

    fun generar(opciones: OpcionesContrasena = OpcionesContrasena()): ResultadoContrasena {
        if (opciones.longitud !in LONGITUD_MINIMA..LONGITUD_MAXIMA) {
            return ResultadoContrasena.Error("La longitud debe estar entre $LONGITUD_MINIMA y $LONGITUD_MAXIMA")
        }
        val grupos = gruposActivos(opciones)
        if (grupos.isEmpty()) return ResultadoContrasena.Error("Activa al menos un tipo de carácter")

        val alfabeto = grupos.joinToString("")
        val caracteres = ArrayList<Char>(opciones.longitud)
        grupos.forEach { caracteres.add(it[aleatorio.nextInt(it.length)]) }          // uno de cada tipo
        while (caracteres.size < opciones.longitud) caracteres.add(alfabeto[aleatorio.nextInt(alfabeto.length)])
        for (i in caracteres.size - 1 downTo 1) {                                    // Fisher-Yates
            val j = aleatorio.nextInt(i + 1)
            val t = caracteres[i]; caracteres[i] = caracteres[j]; caracteres[j] = t
        }
        val entropia = entropiaBits(opciones.longitud, alfabeto.length)
        return ResultadoContrasena.Exito(String(caracteres.toCharArray()), entropia, clasificar(entropia))
    }

    fun entropiaBits(longitud: Int, tamañoAlfabeto: Int): Double =
        longitud * (ln(tamañoAlfabeto.toDouble()) / ln(2.0))

    fun clasificar(entropiaBits: Double): FuerzaContrasena = when {
        entropiaBits < 50  -> FuerzaContrasena.DEBIL
        entropiaBits < 70  -> FuerzaContrasena.ACEPTABLE
        entropiaBits < 100 -> FuerzaContrasena.FUERTE
        else               -> FuerzaContrasena.MUY_FUERTE
    }

    /** Grupos de caracteres activos, ya sin los ambiguos si se pidió. */
    internal fun gruposActivos(o: OpcionesContrasena): List<String> =
        listOfNotNull(
            MINUSCULAS.takeIf { o.minusculas }, MAYUSCULAS.takeIf { o.mayusculas },
            NUMEROS.takeIf { o.numeros },       SIMBOLOS.takeIf { o.simbolos }
        ).map { g -> if (o.excluirAmbiguos) g.filterNot { it in AMBIGUOS } else g }
}
