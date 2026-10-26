package com.criptes.app.criptografia

import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.*

// ============================================================
//  CriptES — Motor de Cifrado Simétrico
//  Implementa: AES-256-GCM, DES, 3DES, ChaCha20-Poly1305
// ============================================================

/**
 * Resultado de una operación criptográfica.
 * Puede ser éxito con el texto resultante, o error con mensaje.
 */
sealed class ResultadoCifrado {
    data class Exito(val texto: String) : ResultadoCifrado()
    data class Error(val mensaje: String) : ResultadoCifrado()
}

/**
 * Enumeración de algoritmos simétricos soportados.
 *
 * @param nombreMostrar Nombre amigable para la UI
 * @param descripcionCorta Descripción breve del algoritmo
 * @param transformacion Especificación técnica para javax.crypto
 */
enum class AlgoritmoSimetrico(
    val nombreMostrar: String,
    val descripcionCorta: String,
    val transformacion: String,
    val longitudClave: Int,   // en bytes
    val tamañoIV: Int,        // IV o nonce, en bytes
    val autenticado: Boolean  // detecta manipulación del texto cifrado
) {
    AES(
        nombreMostrar    = "AES-256",
        descripcionCorta = "AES-256-GCM: estándar actual, cifrado autenticado",
        transformacion   = "AES/GCM/NoPadding",
        longitudClave    = 32,    // 256 bits
        tamañoIV         = 12,
        autenticado      = true
    ),
    DES(
        nombreMostrar    = "DES",
        descripcionCorta = "Obsoleto e inseguro, sin autenticación: solo educativo",
        transformacion   = "DES/CBC/PKCS5Padding",
        longitudClave    = 8,     // 64 bits (56 efectivos)
        tamañoIV         = 8,
        autenticado      = false
    ),
    TRIPLE_DES(
        nombreMostrar    = "3DES",
        descripcionCorta = "Retirado para uso nuevo, sin autenticación: solo educativo",
        transformacion   = "DESede/CBC/PKCS5Padding",
        longitudClave    = 24,    // 192 bits representados
        tamañoIV         = 8,
        autenticado      = false
    ),
    CHACHA20(
        nombreMostrar    = "ChaCha20",
        descripcionCorta = "ChaCha20-Poly1305: moderno, cifrado autenticado (TLS 1.3)",
        transformacion   = "ChaCha20-Poly1305",
        longitudClave    = 32,    // 256 bits
        tamañoIV         = 12,
        autenticado      = true
    )
}

/**
 * Motor principal de cifrado simétrico de CriptES.
 *
 * Todos los métodos son funciones puras sin estado.
 *
 * Formato del texto cifrado (Base64):
 *   [sal de 16 bytes] + [IV o nonce] + [datos cifrados]
 *
 * AES-256-GCM y ChaCha20-Poly1305 añaden una etiqueta de 16 bytes
 * que se verifica al descifrar: cualquier alteración de la sal, el
 * IV/nonce o los datos, o una contraseña incorrecta, produce un error.
 * DES y 3DES (CBC) NO autentican: son demostraciones educativas.
 *
 * Proveedores: AES, DES, 3DES y PBKDF2 usan los proveedores del sistema.
 * ChaCha20-Poly1305 usa la API ligera de Bouncy Castle (referencia
 * directa, sin registrar ni nombrar ningún proveedor), de modo que
 * funciona igual en API 26-27, donde Android no lo incluye.
 */
object CifradoSimetrico {

    private const val TAMAÑO_SAL      = 16
    private const val BITS_ETIQUETA   = 128
    private const val ITERACIONES_KDF = 65_536

    private val aleatorio = SecureRandom()

    // ── API Pública ──────────────────────────────────────────

    /**
     * Cifra un texto plano usando el algoritmo y contraseña dados.
     *
     * @param textoCrudo Texto a cifrar
     * @param contrasena Contraseña del usuario (se deriva a clave criptográfica)
     * @param algoritmo  Algoritmo simétrico a utilizar
     * @return ResultadoCifrado con el texto cifrado en Base64, o Error
     */
    fun cifrar(
        textoCrudo: String,
        contrasena:  String,
        algoritmo:  AlgoritmoSimetrico
    ): ResultadoCifrado {
        if (textoCrudo.isBlank()) return ResultadoCifrado.Error("El texto no puede estar vacío")
        if (contrasena.isBlank()) return ResultadoCifrado.Error("La contraseña no puede estar vacía")

        return try {
            val sal     = bytesAleatorios(TAMAÑO_SAL)
            val iv      = bytesAleatorios(algoritmo.tamañoIV)
            val clave   = derivarClave(contrasena, algoritmo, sal)
            val datos   = textoCrudo.toByteArray(Charsets.UTF_8)
            val cifrado = when (algoritmo) {
                AlgoritmoSimetrico.CHACHA20 -> procesarChaCha20(true, datos, clave, iv)
                else                        -> procesarConCipher(Cipher.ENCRYPT_MODE, datos, clave, iv, algoritmo)
            }
            // Formato: [sal] + [IV/nonce] + [datos cifrados]
            ResultadoCifrado.Exito(Base64.getEncoder().encodeToString(sal + iv + cifrado))
        } catch (e: Exception) {
            ResultadoCifrado.Error("Error al cifrar: ${e.localizedMessage}")
        }
    }

    /**
     * Descifra un texto cifrado en Base64.
     *
     * @param textoCifrado Texto cifrado en Base64 (generado por [cifrar])
     * @param contrasena   La misma contraseña usada al cifrar
     * @param algoritmo    El mismo algoritmo usado al cifrar
     * @return ResultadoCifrado con el texto descifrado, o Error
     */
    fun descifrar(
        textoCifrado: String,
        contrasena:   String,
        algoritmo:    AlgoritmoSimetrico
    ): ResultadoCifrado {
        if (textoCifrado.isBlank()) return ResultadoCifrado.Error("El texto cifrado no puede estar vacío")
        if (contrasena.isBlank())   return ResultadoCifrado.Error("La contraseña no puede estar vacía")

        return try {
            val bytesEntrada = Base64.getDecoder().decode(textoCifrado.trim())
            if (bytesEntrada.size <= TAMAÑO_SAL + algoritmo.tamañoIV) {
                return ResultadoCifrado.Error("Texto cifrado inválido o demasiado corto")
            }
            val sal     = bytesEntrada.copyOfRange(0, TAMAÑO_SAL)
            val iv      = bytesEntrada.copyOfRange(TAMAÑO_SAL, TAMAÑO_SAL + algoritmo.tamañoIV)
            val cifrado = bytesEntrada.copyOfRange(TAMAÑO_SAL + algoritmo.tamañoIV, bytesEntrada.size)
            val clave   = derivarClave(contrasena, algoritmo, sal)
            val datos   = when (algoritmo) {
                AlgoritmoSimetrico.CHACHA20 -> procesarChaCha20(false, cifrado, clave, iv)
                else                        -> procesarConCipher(Cipher.DECRYPT_MODE, cifrado, clave, iv, algoritmo)
            }
            ResultadoCifrado.Exito(String(datos, Charsets.UTF_8))
        } catch (e: Exception) {
            ResultadoCifrado.Error("Error al descifrar — verifica la contraseña y el algoritmo")
        }
    }

    // ── Implementaciones internas ────────────────────────────

    private fun bytesAleatorios(tamaño: Int): ByteArray =
        ByteArray(tamaño).also { aleatorio.nextBytes(it) }

    /**
     * Deriva una clave criptográfica de longitud correcta desde
     * una contraseña de usuario usando PBKDF2 con SHA-256.
     *
     * PBKDF2 (Password-Based Key Derivation Function 2) es el
     * estándar para convertir contraseñas en claves criptográficas.
     * Usa 65536 iteraciones para hacer los ataques de fuerza bruta
     * computacionalmente costosos.
     *
     * @param sal Sal aleatoria de 16 bytes, única por cada operación de cifrado.
     *            Se genera al cifrar y se recupera del texto cifrado al descifrar.
     */
    private fun derivarClave(
        contrasena: String,
        algoritmo:  AlgoritmoSimetrico,
        sal:        ByteArray
    ): ByteArray {
        val fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec    = PBEKeySpec(
            contrasena.toCharArray(),
            sal,
            ITERACIONES_KDF,
            algoritmo.longitudClave * 8  // bits
        )
        try {
            return fabrica.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    /**
     * Cifra o descifra con el proveedor del sistema (AES-GCM, DES-CBC, 3DES-CBC).
     * En GCM, doFinal verifica la etiqueta y lanza excepción si no coincide.
     */
    private fun procesarConCipher(
        modo:      Int,
        datos:     ByteArray,
        clave:     ByteArray,
        iv:        ByteArray,
        algoritmo: AlgoritmoSimetrico
    ): ByteArray {
        val tipoClave = when (algoritmo) {
            AlgoritmoSimetrico.AES        -> "AES"
            AlgoritmoSimetrico.DES        -> "DES"
            AlgoritmoSimetrico.TRIPLE_DES -> "DESede"
            AlgoritmoSimetrico.CHACHA20   -> error("ChaCha20 no usa javax.crypto.Cipher")
        }
        val parametros = if (algoritmo == AlgoritmoSimetrico.AES) GCMParameterSpec(BITS_ETIQUETA, iv)
                         else IvParameterSpec(iv)
        val cipher = Cipher.getInstance(algoritmo.transformacion)
        cipher.init(modo, SecretKeySpec(clave, tipoClave), parametros)
        return cipher.doFinal(datos)
    }

    /**
     * ChaCha20-Poly1305 (RFC 8439) con la API ligera de Bouncy Castle.
     * Al descifrar, doFinal verifica la etiqueta Poly1305 y lanza
     * InvalidCipherTextException si no coincide.
     */
    private fun procesarChaCha20(
        cifrando: Boolean,
        datos:    ByteArray,
        clave:    ByteArray,
        nonce:    ByteArray
    ): ByteArray {
        val aead = ChaCha20Poly1305()
        aead.init(cifrando, ParametersWithIV(KeyParameter(clave), nonce))
        val salida = ByteArray(aead.getOutputSize(datos.size))
        var longitud = aead.processBytes(datos, 0, datos.size, salida, 0)
        longitud += aead.doFinal(salida, longitud)
        return salida.copyOf(longitud)
    }
}
