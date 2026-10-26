package com.criptes.app.criptografia

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.encodings.OAEPEncoding
import org.bouncycastle.crypto.engines.RSAEngine
import org.bouncycastle.crypto.params.RSAKeyParameters
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.security.*
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.*
import java.util.Base64

// ============================================================
//  CriptES — Motor de Cifrado Asimétrico RSA
//  Generación de claves, cifrado/descifrado y formato PEM
// ============================================================

/**
 * Par de claves RSA generado por [CifradoRSA].
 *
 * @param clavePublicaPEM Clave pública en formato PEM (para compartir)
 * @param clavePrivadaPEM Clave privada en formato PEM (guardar con seguridad)
 */
data class ParClavesRSA(
    val clavePublicaPEM:  String,
    val clavePrivadaPEM:  String
)

/**
 * Resultado de operaciones RSA.
 */
sealed class ResultadoRSA {
    data class ExitoCifrado(val textoCifrado: String)    : ResultadoRSA()
    data class ExitoDescifrado(val textoPlano: String)   : ResultadoRSA()
    data class ExitoClaves(val parClaves: ParClavesRSA)  : ResultadoRSA()
    data class Error(val mensaje: String)                : ResultadoRSA()
}

/**
 * Motor de cifrado asimétrico RSA.
 *
 * RSA (Rivest–Shamir–Adleman) usa matemática de números primos
 * para crear un par de claves vinculadas:
 * - 🔓 Clave PÚBLICA: Puede compartirse libremente. Cifra mensajes.
 * - 🔐 Clave PRIVADA: Nunca compartir. Descifra mensajes.
 *
 * Tamaño de clave: 2048 bits (estándar actual seguro).
 * Relleno: OAEP con SHA-256 y MGF1 con SHA-256 (más seguro que PKCS1v1.5).
 *
 * La generación de claves y la lectura de PEM usan el proveedor del
 * sistema. El relleno OAEP se aplica con la API ligera de Bouncy
 * Castle para fijar los parámetros explícitamente (OAEP-SHA-256 con
 * MGF1-SHA-256, sin etiqueta): los proveedores de JDK/Android difieren
 * en el digest de MGF1 por defecto (SHA-1), y así el resultado no
 * depende del proveedor ni de la versión de Android.
 *
 * Esto es compatible con `RSA/ECB/OAEPPadding` de JCA si se le pasa
 * `OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)`.
 */
object CifradoRSA {

    private const val TAMAÑO_CLAVE    = 2048
    private const val ALGORITMO       = "RSA"

    // OAEP-SHA-256: máx = tamañoClave − 2·tamañoHash − 2 = 256 − 64 − 2 = 190 bytes
    private const val MAX_BYTES_TEXTO = TAMAÑO_CLAVE / 8 - 2 * 32 - 2

    // Encabezados PEM estándar
    private const val INICIO_PUBLICA  = "-----BEGIN PUBLIC KEY-----"
    private const val FIN_PUBLICA     = "-----END PUBLIC KEY-----"
    private const val INICIO_PRIVADA  = "-----BEGIN PRIVATE KEY-----"
    private const val FIN_PRIVADA     = "-----END PRIVATE KEY-----"

    // ── API Pública ──────────────────────────────────────────

    /**
     * Genera un nuevo par de claves RSA de 2048 bits.
     *
     * La generación puede tardar 1-3 segundos dependiendo
     * del dispositivo — llamar desde una coroutine.
     *
     * @return ResultadoRSA.ExitoClaves con las claves en formato PEM
     */
    fun generarParClaves(): ResultadoRSA {
        return try {
            val generador = KeyPairGenerator.getInstance(ALGORITMO)
            generador.initialize(TAMAÑO_CLAVE, SecureRandom())
            val par = generador.generateKeyPair()

            ResultadoRSA.ExitoClaves(
                ParClavesRSA(
                    clavePublicaPEM  = convertirAPEM(par.public.encoded, esPublica = true),
                    clavePrivadaPEM  = convertirAPEM(par.private.encoded, esPublica = false)
                )
            )
        } catch (e: Exception) {
            ResultadoRSA.Error("Error generando claves RSA: ${e.localizedMessage}")
        }
    }

    /**
     * Cifra un texto con una clave pública RSA en formato PEM.
     *
     * @param textoCrudo     Texto a cifrar (máx 190 bytes UTF-8 con RSA-2048 y OAEP-SHA-256)
     * @param clavePublicaPEM Clave pública en formato PEM
     * @return ResultadoRSA.ExitoCifrado con el texto cifrado en Base64
     */
    fun cifrar(textoCrudo: String, clavePublicaPEM: String): ResultadoRSA {
        if (textoCrudo.isBlank())      return ResultadoRSA.Error("El texto no puede estar vacío")
        if (clavePublicaPEM.isBlank()) return ResultadoRSA.Error("La clave pública no puede estar vacía")
        val bytesTexto = textoCrudo.toByteArray(Charsets.UTF_8)
        if (bytesTexto.size > MAX_BYTES_TEXTO) return ResultadoRSA.Error(
            "RSA solo puede cifrar textos cortos (máx $MAX_BYTES_TEXTO bytes en UTF-8; " +
            "tu texto ocupa ${bytesTexto.size}). Para textos largos usa AES."
        )

        return try {
            val clavePublica = cargarClavePublica(clavePublicaPEM) as RSAPublicKey
            val oaep = OAEPEncoding(RSAEngine(), SHA256Digest(), SHA256Digest(), null)
            oaep.init(true, RSAKeyParameters(false, clavePublica.modulus, clavePublica.publicExponent))
            val cifrado = oaep.processBlock(bytesTexto, 0, bytesTexto.size)
            ResultadoRSA.ExitoCifrado(Base64.getEncoder().encodeToString(cifrado))
        } catch (e: Exception) {
            ResultadoRSA.Error("Error al cifrar: ${e.localizedMessage}")
        }
    }

    /**
     * Descifra un texto cifrado con la clave privada RSA correspondiente.
     *
     * @param textoCifrado    Texto cifrado en Base64 (generado por [cifrar])
     * @param clavePrivadaPEM Clave privada en formato PEM
     * @return ResultadoRSA.ExitoDescifrado con el texto descifrado
     */
    fun descifrar(textoCifrado: String, clavePrivadaPEM: String): ResultadoRSA {
        if (textoCifrado.isBlank())     return ResultadoRSA.Error("El texto cifrado no puede estar vacío")
        if (clavePrivadaPEM.isBlank())  return ResultadoRSA.Error("La clave privada no puede estar vacía")

        return try {
            val clavePrivada = cargarClavePrivada(clavePrivadaPEM) as RSAPrivateCrtKey
            val oaep = OAEPEncoding(RSAEngine(), SHA256Digest(), SHA256Digest(), null)
            oaep.init(false, RSAPrivateCrtKeyParameters(
                clavePrivada.modulus, clavePrivada.publicExponent, clavePrivada.privateExponent,
                clavePrivada.primeP, clavePrivada.primeQ,
                clavePrivada.primeExponentP, clavePrivada.primeExponentQ, clavePrivada.crtCoefficient
            ))
            val cifrado    = Base64.getDecoder().decode(textoCifrado.filterNot { it.isWhitespace() })
            val descifrado = oaep.processBlock(cifrado, 0, cifrado.size)
            ResultadoRSA.ExitoDescifrado(String(descifrado, Charsets.UTF_8))
        } catch (e: Exception) {
            ResultadoRSA.Error("Error al descifrar — verifica que uses la clave privada correcta")
        }
    }

    // ── Utilidades PEM ───────────────────────────────────────

    /**
     * Convierte bytes de clave a formato PEM estándar.
     * PEM = Base64 codificado con encabezados identificadores.
     */
    private fun convertirAPEM(bytesClaveRaw: ByteArray, esPublica: Boolean): String {
        val base64   = Base64.getEncoder().encodeToString(bytesClaveRaw)
        val lineas   = base64.chunked(64).joinToString("\n")
        val inicio   = if (esPublica) INICIO_PUBLICA else INICIO_PRIVADA
        val fin      = if (esPublica) FIN_PUBLICA else FIN_PRIVADA
        return "$inicio\n$lineas\n$fin"
    }

    /**
     * Carga una clave pública desde formato PEM.
     */
    private fun cargarClavePublica(pem: String): PublicKey {
        val contenido = pem
            .replace(INICIO_PUBLICA, "")
            .replace(FIN_PUBLICA, "")
            .filterNot { it.isWhitespace() }
        val bytesClaveRaw = Base64.getDecoder().decode(contenido)
        val spec     = X509EncodedKeySpec(bytesClaveRaw)
        val fabrica  = KeyFactory.getInstance(ALGORITMO)
        return fabrica.generatePublic(spec)
    }

    /**
     * Carga una clave privada desde formato PEM.
     */
    private fun cargarClavePrivada(pem: String): PrivateKey {
        val contenido = pem
            .replace(INICIO_PRIVADA, "")
            .replace(FIN_PRIVADA, "")
            .filterNot { it.isWhitespace() }
        val bytesClaveRaw = Base64.getDecoder().decode(contenido)
        val spec     = PKCS8EncodedKeySpec(bytesClaveRaw)
        val fabrica  = KeyFactory.getInstance(ALGORITMO)
        return fabrica.generatePrivate(spec)
    }
}
