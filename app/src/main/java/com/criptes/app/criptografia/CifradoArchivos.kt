package com.criptes.app.criptografia

import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

// ============================================================
//  CriptES — Cifrado de archivos (AES-256-GCM por bloques)
// ============================================================

/**
 * Resultado de cifrar o descifrar un archivo.
 */
sealed class ResultadoArchivo {
    data class Exito(val bytesProcesados: Long) : ResultadoArchivo()
    data class Error(val mensaje: String)       : ResultadoArchivo()
}

/**
 * Cifrado de archivos con AES-256-GCM en bloques de 64 KiB, para no
 * cargar el archivo entero en memoria.
 *
 * Formato:
 *   "CRPF" | versión (1) | sal (16) | prefijo de nonce (4) |
 *   { longitud del bloque cifrado (4, big-endian) | bloque cifrado + etiqueta (16) }...
 *
 * - Clave: PBKDF2-HMAC-SHA-256 (misma derivación que el cifrado de texto).
 * - Nonce de cada bloque: prefijo aleatorio (4) + contador de bloque (8).
 * - Dato autenticado adicional (AAD): 1 byte, 1 si es el último bloque.
 *   Así se detectan bloques reordenados, eliminados o truncados al final.
 * - El descifrado solo escribe en la salida bloques ya autenticados, pero un
 *   archivo manipulado puede dejar una salida parcial: quien llama debe
 *   descartarla si el resultado es Error.
 */
object CifradoArchivos {

    private val MAGIA          = byteArrayOf('C'.code.toByte(), 'R'.code.toByte(), 'P'.code.toByte(), 'F'.code.toByte())
    private const val VERSION  = 1
    private const val TAMAÑO_SAL     = 16
    private const val TAMAÑO_PREFIJO = 4
    private const val TAMAÑO_BLOQUE  = 64 * 1024
    private const val BYTES_ETIQUETA = 16
    private const val BITS_ETIQUETA  = 128
    private const val LONGITUD_CLAVE = 32

    /** Extensión añadida a los archivos cifrados. */
    const val EXTENSION = ".criptes"

    fun cifrar(entrada: InputStream, salida: OutputStream, contrasena: String): ResultadoArchivo {
        if (contrasena.isBlank()) return ResultadoArchivo.Error("La contraseña no puede estar vacía")
        return try {
            val aleatorio = SecureRandom()
            val sal       = ByteArray(TAMAÑO_SAL).also { aleatorio.nextBytes(it) }
            val prefijo   = ByteArray(TAMAÑO_PREFIJO).also { aleatorio.nextBytes(it) }
            val clave     = SecretKeySpec(CifradoSimetrico.derivarClave(contrasena, LONGITUD_CLAVE, sal), "AES")

            salida.write(MAGIA)
            salida.write(VERSION)
            salida.write(sal)
            salida.write(prefijo)

            var total   = 0L
            var contador = 0L
            var actual  = leerBloque(entrada, TAMAÑO_BLOQUE)
            while (true) {
                val siguiente = if (actual.size == TAMAÑO_BLOQUE) leerBloque(entrada, TAMAÑO_BLOQUE) else ByteArray(0)
                val esUltimo  = siguiente.isEmpty()
                val cifrado   = procesarBloque(Cipher.ENCRYPT_MODE, clave, prefijo, contador, esUltimo, actual)
                salida.write(enteroABytes(cifrado.size))
                salida.write(cifrado)
                total += actual.size
                if (esUltimo) break
                actual = siguiente
                contador++
            }
            salida.flush()
            ResultadoArchivo.Exito(total)
        } catch (e: Exception) {
            ResultadoArchivo.Error("Error al cifrar el archivo: ${e.localizedMessage}")
        }
    }

    fun descifrar(entrada: InputStream, salida: OutputStream, contrasena: String): ResultadoArchivo {
        if (contrasena.isBlank()) return ResultadoArchivo.Error("La contraseña no puede estar vacía")
        val mensajeGenerico = "Error al descifrar — verifica la contraseña o que el archivo no esté dañado"
        return try {
            val datos = DataInputStream(entrada)
            val magia = ByteArray(MAGIA.size).also { datos.readFully(it) }
            if (!magia.contentEquals(MAGIA)) return ResultadoArchivo.Error("No es un archivo cifrado con CriptES")
            if (datos.read() != VERSION) return ResultadoArchivo.Error("Versión de archivo no soportada")
            val sal     = ByteArray(TAMAÑO_SAL).also { datos.readFully(it) }
            val prefijo = ByteArray(TAMAÑO_PREFIJO).also { datos.readFully(it) }
            val clave   = SecretKeySpec(CifradoSimetrico.derivarClave(contrasena, LONGITUD_CLAVE, sal), "AES")

            var total    = 0L
            var contador = 0L
            var actual   = leerBloqueCifrado(datos) ?: return ResultadoArchivo.Error(mensajeGenerico)
            while (true) {
                val siguiente = leerBloqueCifrado(datos)
                val esUltimo  = siguiente == null
                val plano     = procesarBloque(Cipher.DECRYPT_MODE, clave, prefijo, contador, esUltimo, actual)
                salida.write(plano)
                total += plano.size
                if (esUltimo) break
                actual = siguiente!!
                contador++
            }
            salida.flush()
            ResultadoArchivo.Exito(total)
        } catch (e: Exception) {
            ResultadoArchivo.Error(mensajeGenerico)
        }
    }

    /** Nombre sugerido para el archivo cifrado. */
    fun nombreCifrado(original: String) = original + EXTENSION

    /** Nombre sugerido para el archivo descifrado. */
    fun nombreDescifrado(cifrado: String) =
        if (cifrado.endsWith(EXTENSION) && cifrado.length > EXTENSION.length) cifrado.removeSuffix(EXTENSION)
        else "$cifrado.descifrado"

    // ── Internos ─────────────────────────────────────────────

    private fun procesarBloque(
        modo: Int, clave: SecretKeySpec, prefijo: ByteArray,
        contador: Long, esUltimo: Boolean, datos: ByteArray
    ): ByteArray {
        val nonce = ByteArray(12)
        System.arraycopy(prefijo, 0, nonce, 0, TAMAÑO_PREFIJO)
        for (i in 0 until 8) nonce[TAMAÑO_PREFIJO + i] = (contador ushr (56 - 8 * i)).toByte()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(modo, clave, GCMParameterSpec(BITS_ETIQUETA, nonce))
        cipher.updateAAD(byteArrayOf(if (esUltimo) 1 else 0))
        return cipher.doFinal(datos)
    }

    /** Lee hasta [maximo] bytes; devuelve un array vacío solo si ya no hay datos. */
    private fun leerBloque(entrada: InputStream, maximo: Int): ByteArray {
        val buffer = ByteArray(maximo)
        var leidos = 0
        while (leidos < maximo) {
            val n = entrada.read(buffer, leidos, maximo - leidos)
            if (n < 0) break
            leidos += n
        }
        return if (leidos == maximo) buffer else buffer.copyOf(leidos)
    }

    /** Lee un bloque cifrado con su prefijo de longitud; null si no quedan datos. */
    private fun leerBloqueCifrado(entrada: DataInputStream): ByteArray? {
        val primero = entrada.read()
        if (primero < 0) return null
        val resto = ByteArray(3).also { entrada.readFully(it) }
        val longitud = (primero shl 24) or ((resto[0].toInt() and 0xFF) shl 16) or
                       ((resto[1].toInt() and 0xFF) shl 8) or (resto[2].toInt() and 0xFF)
        if (longitud < BYTES_ETIQUETA || longitud > TAMAÑO_BLOQUE + BYTES_ETIQUETA) {
            throw IOException("Longitud de bloque inválida")
        }
        return ByteArray(longitud).also { entrada.readFully(it) }
    }

    private fun enteroABytes(valor: Int) = byteArrayOf(
        (valor ushr 24).toByte(), (valor ushr 16).toByte(), (valor ushr 8).toByte(), valor.toByte()
    )
}
