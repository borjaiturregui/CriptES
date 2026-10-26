package com.criptes.app.criptografia

import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.security.KeyFactory
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

class CifradoRSATest {

    companion object {
        lateinit var claves: ParClavesRSA

        @BeforeClass @JvmStatic fun generar() {
            claves = (CifradoRSA.generarParClaves() as ResultadoRSA.ExitoClaves).parClaves
        }
    }

    private fun cifrar(texto: String, publica: String = claves.clavePublicaPEM) =
        (CifradoRSA.cifrar(texto, publica) as ResultadoRSA.ExitoCifrado).textoCifrado

    private val oaepSha256 = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)

    @Test fun clavesGeneradas_tienenFormatoPEM() {
        assertTrue(claves.clavePublicaPEM.startsWith("-----BEGIN PUBLIC KEY-----"))
        assertTrue(claves.clavePublicaPEM.endsWith("-----END PUBLIC KEY-----"))
        assertTrue(claves.clavePrivadaPEM.startsWith("-----BEGIN PRIVATE KEY-----"))
        assertTrue(claves.clavePrivadaPEM.endsWith("-----END PRIVATE KEY-----"))
    }

    @Test fun roundtrip_devuelveTextoOriginal() {
        val r = CifradoRSA.descifrar(cifrar("Hola RSA"), claves.clavePrivadaPEM)
        assertEquals("Hola RSA", (r as ResultadoRSA.ExitoDescifrado).textoPlano)
    }

    @Test fun roundtrip_unicode() {
        val t = "Contraseñas 中文 🔐"
        val r = CifradoRSA.descifrar(cifrar(t), claves.clavePrivadaPEM)
        assertEquals(t, (r as ResultadoRSA.ExitoDescifrado).textoPlano)
    }

    @Test fun cifrarDosVeces_producenResultadosDistintos() {
        assertNotEquals(cifrar("igual"), cifrar("igual"))
    }

    @Test fun clavePrivadaIncorrecta_devuelveError() {
        val otra = (CifradoRSA.generarParClaves() as ResultadoRSA.ExitoClaves).parClaves
        assertTrue(CifradoRSA.descifrar(cifrar("secreto"), otra.clavePrivadaPEM) is ResultadoRSA.Error)
    }

    @Test fun textoCifradoAlterado_devuelveError() {
        val bytes = Base64.getDecoder().decode(cifrar("secreto"))
        bytes[bytes.size / 2] = (bytes[bytes.size / 2].toInt() xor 1).toByte()
        val r = CifradoRSA.descifrar(Base64.getEncoder().encodeToString(bytes), claves.clavePrivadaPEM)
        assertTrue(r is ResultadoRSA.Error)
    }

    @Test fun limiteEs190BytesUtf8_noCaracteres() {
        assertTrue(CifradoRSA.cifrar("a".repeat(190), claves.clavePublicaPEM) is ResultadoRSA.ExitoCifrado)
        assertTrue(CifradoRSA.cifrar("a".repeat(191), claves.clavePublicaPEM) is ResultadoRSA.Error)
        // 100 caracteres de 2 bytes (ñ) = 200 bytes > 190
        assertTrue(CifradoRSA.cifrar("ñ".repeat(100), claves.clavePublicaPEM) is ResultadoRSA.Error)
        val r = CifradoRSA.descifrar(cifrar("a".repeat(190)), claves.clavePrivadaPEM)
        assertEquals("a".repeat(190), (r as ResultadoRSA.ExitoDescifrado).textoPlano)
    }

    @Test fun entradasInvalidas_devuelvenError() {
        assertTrue(CifradoRSA.cifrar("", claves.clavePublicaPEM) is ResultadoRSA.Error)
        assertTrue(CifradoRSA.cifrar("x", "") is ResultadoRSA.Error)
        assertTrue(CifradoRSA.cifrar("x", "-----BEGIN PUBLIC KEY-----\n%%%\n-----END PUBLIC KEY-----") is ResultadoRSA.Error)
        assertTrue(CifradoRSA.descifrar("", claves.clavePrivadaPEM) is ResultadoRSA.Error)
        assertTrue(CifradoRSA.descifrar(cifrar("x"), "") is ResultadoRSA.Error)
        assertTrue(CifradoRSA.descifrar("%%%", claves.clavePrivadaPEM) is ResultadoRSA.Error)
        assertTrue(CifradoRSA.descifrar(cifrar("x"), "-----BEGIN PRIVATE KEY-----\nAAAA\n-----END PRIVATE KEY-----") is ResultadoRSA.Error)
    }

    @Test fun pemConSaltosCRLF_seAcepta() {
        val crlf = claves.clavePublicaPEM.replace("\n", "\r\n")
        val r = CifradoRSA.descifrar(cifrar("crlf", crlf), claves.clavePrivadaPEM.replace("\n", "\r\n"))
        assertEquals("crlf", (r as ResultadoRSA.ExitoDescifrado).textoPlano)
    }

    // ── Interoperabilidad: parámetros OAEP explícitos (SHA-256 + MGF1-SHA-256) ──

    private fun pemABytes(pem: String) =
        Base64.getDecoder().decode(pem.lines().filterNot { it.startsWith("-----") }.joinToString(""))

    @Test fun interop_nuestroCifradoSeDescifraConJCAConOAEPExplicito() {
        val privada = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(pemABytes(claves.clavePrivadaPEM)))
        val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
        cipher.init(Cipher.DECRYPT_MODE, privada, oaepSha256)
        val plano = cipher.doFinal(Base64.getDecoder().decode(cifrar("interop")))
        assertEquals("interop", String(plano, Charsets.UTF_8))
    }

    @Test fun interop_cifradoJCAConOAEPExplicitoSeDescifraConNuestroCodigo() {
        val publica = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(pemABytes(claves.clavePublicaPEM)))
        val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
        cipher.init(Cipher.ENCRYPT_MODE, publica, oaepSha256)
        val cifrado = Base64.getEncoder().encodeToString(cipher.doFinal("desde JCA".toByteArray()))
        val r = CifradoRSA.descifrar(cifrado, claves.clavePrivadaPEM)
        assertEquals("desde JCA", (r as ResultadoRSA.ExitoDescifrado).textoPlano)
    }
}
