package com.criptes.app.criptografia

import java.security.MessageDigest

// ============================================================
//  CriptES — Generador de Hashes Criptográficos
//  Implementa: MD5, SHA-1, SHA-256, SHA-512
// ============================================================

/**
 * Algoritmos de hash soportados por CriptES.
 *
 * @param algoritmo    Nombre técnico para MessageDigest de Java
 * @param nombreMostrar Nombre amigable para la UI
 * @param bits         Longitud del hash en bits
 * @param eSeguro      Si se considera seguro para uso criptográfico actual
 */
enum class AlgoritmoHash(
    val algoritmo:      String,
    val nombreMostrar:  String,
    val bits:           Int,
    val esSeguro:       Boolean
) {
    MD5(
        algoritmo     = "MD5",
        nombreMostrar = "MD5",
        bits          = 128,
        esSeguro      = false   // Colisiones conocidas desde 2004
    ),
    SHA1(
        algoritmo     = "SHA-1",
        nombreMostrar = "SHA-1",
        bits          = 160,
        esSeguro      = false   // Colisiones demostradas en 2017 (SHAttered)
    ),
    SHA256(
        algoritmo     = "SHA-256",
        nombreMostrar = "SHA-256",
        bits          = 256,
        esSeguro      = true    // Estándar actual, usado en Bitcoin
    ),
    SHA512(
        algoritmo     = "SHA-512",
        nombreMostrar = "SHA-512",
        bits          = 512,
        esSeguro      = true    // Máxima seguridad disponible aquí
    )
}

/**
 * Resultado de la generación de un hash.
 */
sealed class ResultadoHash {
    /** Hash generado exitosamente en formato hexadecimal */
    data class Exito(
        val hash:        String,        // Valor hexadecimal del hash
        val algoritmo:   AlgoritmoHash, // Algoritmo usado
        val longitudHex: Int            // Longitud en caracteres hex
    ) : ResultadoHash()

    data class Error(val mensaje: String) : ResultadoHash()
}

/**
 * Motor generador de hashes criptográficos.
 *
 * Una función de hash toma cualquier entrada y produce
 * una salida de longitud fija (huella digital).
 *
 * Propiedades de un buen hash:
 * - Determinista: misma entrada → mismo hash siempre
 * - Unidireccional: imposible revertir el hash al texto original
 * - Efecto avalancha: un cambio mínimo cambia el hash completamente
 * - Sin colisiones: dos entradas distintas → hashes distintos (idealmente)
 */
object GeneradorHash {

    /**
     * Genera el hash de un texto con el algoritmo especificado.
     *
     * @param texto     Texto a hashear
     * @param algoritmo Algoritmo de hash a usar
     * @return ResultadoHash.Exito con el hash en hexadecimal
     */
    fun generar(texto: String, algoritmo: AlgoritmoHash): ResultadoHash {
        if (texto.isEmpty()) return ResultadoHash.Error("El texto no puede estar vacío")

        return try {
            val digest = MessageDigest.getInstance(algoritmo.algoritmo)
            val bytes  = digest.digest(texto.toByteArray(Charsets.UTF_8))
            val hex    = bytes.joinToString("") { "%02x".format(it) }

            ResultadoHash.Exito(
                hash        = hex,
                algoritmo   = algoritmo,
                longitudHex = hex.length
            )
        } catch (e: Exception) {
            ResultadoHash.Error("Error generando hash: ${e.localizedMessage}")
        }
    }

    /**
     * Genera todos los hashes disponibles para un texto.
     * Útil para mostrar comparativa de todos los algoritmos.
     *
     * @param texto Texto a hashear
     * @return Mapa de AlgoritmoHash → String con el hash (o mensaje de error)
     */
    fun generarTodos(texto: String): Map<AlgoritmoHash, String> {
        return AlgoritmoHash.values().associate { algoritmo ->
            val resultado = generar(texto, algoritmo)
            algoritmo to when (resultado) {
                is ResultadoHash.Exito -> resultado.hash
                is ResultadoHash.Error -> "ERROR"
            }
        }
    }

    /**
     * Verifica si un texto coincide con un hash dado.
     * Usa comparación en tiempo constante para evitar ataques de temporización.
     *
     * @param texto     Texto original
     * @param hash      Hash a comparar (en hexadecimal)
     * @param algoritmo Algoritmo usado para generar el hash
     * @return true si el hash coincide con el texto
     */
    fun verificar(texto: String, hash: String, algoritmo: AlgoritmoHash): Boolean {
        val resultado = generar(texto, algoritmo)
        return when (resultado) {
            is ResultadoHash.Exito -> MessageDigest.isEqual(
                resultado.hash.toByteArray(),
                hash.lowercase().toByteArray()
            )
            is ResultadoHash.Error -> false
        }
    }

    /**
     * Intenta detectar el tipo de hash basándose en su longitud.
     * No es infalible, pero es útil como primera aproximación.
     *
     * @param hash Cadena hexadecimal del hash
     * @return Lista de algoritmos posibles basados en la longitud
     */
    fun detectarTipo(hash: String): List<AlgoritmoHash> {
        val longitudLimpia = hash.trim().lowercase()
        if (!longitudLimpia.all { it.isDigit() || it in 'a'..'f' }) {
            return emptyList()  // No es hexadecimal válido
        }

        return when (longitudLimpia.length) {
            32   -> listOf(AlgoritmoHash.MD5)
            40   -> listOf(AlgoritmoHash.SHA1)
            64   -> listOf(AlgoritmoHash.SHA256)
            128  -> listOf(AlgoritmoHash.SHA512)
            else -> emptyList()
        }
    }
}
