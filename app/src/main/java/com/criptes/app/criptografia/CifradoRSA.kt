package com.criptes.app.criptografia

import android.util.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.*
import java.security.spec.*
import javax.crypto.Cipher

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
 * Relleno: OAEP con SHA-256 (más seguro que PKCS1v1.5).
 */
object CifradoRSA {

    private const val TAMAÑO_CLAVE    = 2048
    private const val ALGORITMO       = "RSA"
    private const val TRANSFORMACION  = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    private const val PROVEEDOR       = "BC"

    // Encabezados PEM estándar
    private const val INICIO_PUBLICA  = "-----BEGIN PUBLIC KEY-----"
    private const val FIN_PUBLICA     = "-----END PUBLIC KEY-----"
    private const val INICIO_PRIVADA  = "-----BEGIN PRIVATE KEY-----"
    private const val FIN_PRIVADA     = "-----END PRIVATE KEY-----"

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

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
            val generador = KeyPairGenerator.getInstance(ALGORITMO, PROVEEDOR)
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
     * @param textoCrudo     Texto a cifrar (máx ~245 bytes con RSA-2048)
     * @param clavePublicaPEM Clave pública en formato PEM
     * @return ResultadoRSA.ExitoCifrado con el texto cifrado en Base64
     */
    fun cifrar(textoCrudo: String, clavePublicaPEM: String): ResultadoRSA {
        if (textoCrudo.isBlank())      return ResultadoRSA.Error("El texto no puede estar vacío")
        if (clavePublicaPEM.isBlank()) return ResultadoRSA.Error("La clave pública no puede estar vacía")
        if (textoCrudo.length > 200)   return ResultadoRSA.Error(
            "RSA solo puede cifrar textos cortos (máx 200 caracteres). Para textos largos usa AES."
        )

        return try {
            val clavePublica = cargarClavePublica(clavePublicaPEM)
            val cipher       = Cipher.getInstance(TRANSFORMACION, PROVEEDOR)
            cipher.init(Cipher.ENCRYPT_MODE, clavePublica)
            val cifrado = cipher.doFinal(textoCrudo.toByteArray(Charsets.UTF_8))
            ResultadoRSA.ExitoCifrado(Base64.encodeToString(cifrado, Base64.NO_WRAP))
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
            val clavePrivada = cargarClavePrivada(clavePrivadaPEM)
            val cipher       = Cipher.getInstance(TRANSFORMACION, PROVEEDOR)
            cipher.init(Cipher.DECRYPT_MODE, clavePrivada)
            val descifrado = cipher.doFinal(Base64.decode(textoCifrado, Base64.NO_WRAP))
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
        val base64   = Base64.encodeToString(bytesClaveRaw, Base64.NO_WRAP)
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
            .replace("\n", "")
            .trim()
        val bytesClaveRaw = Base64.decode(contenido, Base64.NO_WRAP)
        val spec     = X509EncodedKeySpec(bytesClaveRaw)
        val fabrica  = KeyFactory.getInstance(ALGORITMO, PROVEEDOR)
        return fabrica.generatePublic(spec)
    }

    /**
     * Carga una clave privada desde formato PEM.
     */
    private fun cargarClavePrivada(pem: String): PrivateKey {
        val contenido = pem
            .replace(INICIO_PRIVADA, "")
            .replace(FIN_PRIVADA, "")
            .replace("\n", "")
            .trim()
        val bytesClaveRaw = Base64.decode(contenido, Base64.NO_WRAP)
        val spec     = PKCS8EncodedKeySpec(bytesClaveRaw)
        val fabrica  = KeyFactory.getInstance(ALGORITMO, PROVEEDOR)
        return fabrica.generatePrivate(spec)
    }
}
