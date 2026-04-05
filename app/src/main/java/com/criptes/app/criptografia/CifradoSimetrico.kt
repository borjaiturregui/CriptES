package com.criptes.app.criptografia

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.*

// ============================================================
//  CriptES — Motor de Cifrado Simétrico
//  Implementa: AES-256, DES, 3DES, ChaCha20
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
    val longitudClave: Int    // en bytes
) {
    AES(
        nombreMostrar    = "AES-256",
        descripcionCorta = "Estándar actual más seguro",
        transformacion   = "AES/CBC/PKCS5Padding",
        longitudClave    = 32     // 256 bits
    ),
    DES(
        nombreMostrar    = "DES",
        descripcionCorta = "Clásico, actualmente obsoleto",
        transformacion   = "DES/CBC/PKCS5Padding",
        longitudClave    = 8      // 64 bits
    ),
    TRIPLE_DES(
        nombreMostrar    = "3DES",
        descripcionCorta = "Triple DES, más seguro que DES",
        transformacion   = "DESede/CBC/PKCS5Padding",
        longitudClave    = 24     // 192 bits
    ),
    CHACHA20(
        nombreMostrar    = "ChaCha20",
        descripcionCorta = "Moderno y rápido, usado en TLS 1.3",
        transformacion   = "ChaCha20",
        longitudClave    = 32     // 256 bits
    )
}

/**
 * Motor principal de cifrado simétrico de CriptES.
 *
 * Todos los métodos son funciones puras sin estado.
 * El IV (vector de inicialización) se genera aleatoriamente
 * y se adjunta al texto cifrado para permitir el descifrado.
 *
 * Formato del texto cifrado (Base64):
 *   [IV de 16 bytes] + [datos cifrados]
 */
object CifradoSimetrico {

    // Registrar Bouncy Castle como proveedor de seguridad
    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    private val TAMAÑO_SAL     = 16
    private val TAMAÑO_NONCE_CHACHA20 = 12

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
            val sal       = java.security.SecureRandom().generateSeed(TAMAÑO_SAL)
            val clave     = derivarClave(contrasena, algoritmo, sal)
            val cifrado   = when (algoritmo) {
                AlgoritmoSimetrico.CHACHA20    -> cifrarChaCha20(textoCrudo.toByteArray(), clave)
                else                           -> cifrarConIV(textoCrudo.toByteArray(), clave, algoritmo)
            }
            // Formato: [sal de 16 bytes] + [datos cifrados (con IV/nonce incluido)]
            val resultado = sal + cifrado
            ResultadoCifrado.Exito(java.util.Base64.getEncoder().encodeToString(resultado))
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
            val bytesEntrada = java.util.Base64.getDecoder().decode(textoCifrado)
            if (bytesEntrada.size <= TAMAÑO_SAL) {
                return ResultadoCifrado.Error("Texto cifrado inválido o demasiado corto")
            }
            val sal          = bytesEntrada.take(TAMAÑO_SAL).toByteArray()
            val datosCifrados = bytesEntrada.drop(TAMAÑO_SAL).toByteArray()
            val clave        = derivarClave(contrasena, algoritmo, sal)
            val resultado    = when (algoritmo) {
                AlgoritmoSimetrico.CHACHA20 -> descifrarChaCha20(datosCifrados, clave)
                else                        -> descifrarConIV(datosCifrados, clave, algoritmo)
            }
            ResultadoCifrado.Exito(String(resultado))
        } catch (e: Exception) {
            ResultadoCifrado.Error("Error al descifrar — verifica la contraseña y el algoritmo")
        }
    }

    // ── Implementaciones internas ────────────────────────────

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
     *            Debe generarse con SecureRandom al cifrar y recuperarse del
     *            ciphertext al descifrar.
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
            65_536,                      // iteraciones
            algoritmo.longitudClave * 8  // bits
        )
        return fabrica.generateSecret(spec).encoded
    }

    /**
     * Cifra con IV aleatorio (para AES, DES, 3DES).
     * El IV se antepone al texto cifrado para que el descifrado
     * pueda recuperarlo.
     */
    private fun cifrarConIV(
        datos:     ByteArray,
        clave:     ByteArray,
        algoritmo: AlgoritmoSimetrico
    ): ByteArray {
        val cipher    = Cipher.getInstance(algoritmo.transformacion, "BC")
        val secretKey = crearClaveSecreta(clave, algoritmo)

        // Generar IV aleatorio
        val tamañoIV = if (algoritmo == AlgoritmoSimetrico.DES) 8 else 16
        val iv        = java.security.SecureRandom().generateSeed(tamañoIV)
        val ivSpec    = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val cifrado = cipher.doFinal(datos)

        // Formato: [IV][datos cifrados]
        return iv + cifrado
    }

    /**
     * Descifra extrayendo el IV del inicio de los datos.
     */
    private fun descifrarConIV(
        datos:     ByteArray,
        clave:     ByteArray,
        algoritmo: AlgoritmoSimetrico
    ): ByteArray {
        val tamañoIV  = if (algoritmo == AlgoritmoSimetrico.DES) 8 else 16
        val iv        = datos.take(tamañoIV).toByteArray()
        val cifrado   = datos.drop(tamañoIV).toByteArray()

        val cipher    = Cipher.getInstance(algoritmo.transformacion, "BC")
        val secretKey = crearClaveSecreta(clave, algoritmo)
        val ivSpec    = IvParameterSpec(iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        return cipher.doFinal(cifrado)
    }

    /**
     * Cifra con ChaCha20 usando un nonce aleatorio por operación.
     * Formato de salida: [nonce de 12 bytes] + [datos cifrados]
     */
    private fun cifrarChaCha20(datos: ByteArray, clave: ByteArray): ByteArray {
        val nonce     = java.security.SecureRandom().generateSeed(TAMAÑO_NONCE_CHACHA20)
        val cipher    = Cipher.getInstance("ChaCha20", "BC")
        val claveSpec = SecretKeySpec(clave, "ChaCha20")
        val ivSpec    = IvParameterSpec(nonce)
        cipher.init(Cipher.ENCRYPT_MODE, claveSpec, ivSpec)
        return nonce + cipher.doFinal(datos)
    }

    /**
     * Descifra con ChaCha20 extrayendo el nonce del inicio de los datos.
     */
    private fun descifrarChaCha20(datos: ByteArray, clave: ByteArray): ByteArray {
        val nonce     = datos.take(TAMAÑO_NONCE_CHACHA20).toByteArray()
        val cifrado   = datos.drop(TAMAÑO_NONCE_CHACHA20).toByteArray()
        val cipher    = Cipher.getInstance("ChaCha20", "BC")
        val claveSpec = SecretKeySpec(clave, "ChaCha20")
        val ivSpec    = IvParameterSpec(nonce)
        cipher.init(Cipher.DECRYPT_MODE, claveSpec, ivSpec)
        return cipher.doFinal(cifrado)
    }

    /**
     * Crea el objeto SecretKey con el tipo correcto para cada algoritmo.
     */
    private fun crearClaveSecreta(
        clave:     ByteArray,
        algoritmo: AlgoritmoSimetrico
    ): javax.crypto.SecretKey {
        val tipo = when (algoritmo) {
            AlgoritmoSimetrico.AES        -> "AES"
            AlgoritmoSimetrico.DES        -> "DES"
            AlgoritmoSimetrico.TRIPLE_DES -> "DESede"
            AlgoritmoSimetrico.CHACHA20   -> "ChaCha20"
        }
        return SecretKeySpec(clave, tipo)
    }
}
