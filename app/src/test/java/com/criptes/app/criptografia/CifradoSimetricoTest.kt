package com.criptes.app.criptografia

import org.junit.Assert.*
import org.junit.Test

class CifradoSimetricoTest {

    private val texto      = "Hola Mundo CriptES"
    private val contrasena = "contraseña-secreta-123"

    // ── Roundtrip: cifrar y descifrar devuelve el original ───

    @Test fun aes_cifrarDescifrar_devuelveTextoOriginal() {
        val cifrado = CifradoSimetrico.cifrar(texto, contrasena, AlgoritmoSimetrico.AES)
        assertTrue(cifrado is ResultadoCifrado.Exito)
        val descifrado = CifradoSimetrico.descifrar((cifrado as ResultadoCifrado.Exito).texto, contrasena, AlgoritmoSimetrico.AES)
        assertTrue(descifrado is ResultadoCifrado.Exito)
        assertEquals(texto, (descifrado as ResultadoCifrado.Exito).texto)
    }

    @Test fun des_cifrarDescifrar_devuelveTextoOriginal() {
        val cifrado = CifradoSimetrico.cifrar(texto, contrasena, AlgoritmoSimetrico.DES)
        assertTrue(cifrado is ResultadoCifrado.Exito)
        val descifrado = CifradoSimetrico.descifrar((cifrado as ResultadoCifrado.Exito).texto, contrasena, AlgoritmoSimetrico.DES)
        assertTrue(descifrado is ResultadoCifrado.Exito)
        assertEquals(texto, (descifrado as ResultadoCifrado.Exito).texto)
    }

    @Test fun tripleDes_cifrarDescifrar_devuelveTextoOriginal() {
        val cifrado = CifradoSimetrico.cifrar(texto, contrasena, AlgoritmoSimetrico.TRIPLE_DES)
        assertTrue(cifrado is ResultadoCifrado.Exito)
        val descifrado = CifradoSimetrico.descifrar((cifrado as ResultadoCifrado.Exito).texto, contrasena, AlgoritmoSimetrico.TRIPLE_DES)
        assertTrue(descifrado is ResultadoCifrado.Exito)
        assertEquals(texto, (descifrado as ResultadoCifrado.Exito).texto)
    }

    @Test fun chacha20_cifrarDescifrar_devuelveTextoOriginal() {
        val cifrado = CifradoSimetrico.cifrar(texto, contrasena, AlgoritmoSimetrico.CHACHA20)
        assertTrue(cifrado is ResultadoCifrado.Exito)
        val descifrado = CifradoSimetrico.descifrar((cifrado as ResultadoCifrado.Exito).texto, contrasena, AlgoritmoSimetrico.CHACHA20)
        assertTrue(descifrado is ResultadoCifrado.Exito)
        assertEquals(texto, (descifrado as ResultadoCifrado.Exito).texto)
    }

    // ── Sal y nonce aleatorios: dos cifrados distintos ───────

    @Test fun aes_dosCifradosConMismaClaveProducenResultadosDiferentes() {
        val c1 = (CifradoSimetrico.cifrar(texto, contrasena, AlgoritmoSimetrico.AES) as ResultadoCifrado.Exito).texto
        val c2 = (CifradoSimetrico.cifrar(texto, contrasena, AlgoritmoSimetrico.AES) as ResultadoCifrado.Exito).texto
        assertNotEquals("Mismo ciphertext indica sal o IV reutilizado", c1, c2)
    }

    @Test fun chacha20_dosCifradosConMismaClaveProducenResultadosDiferentes() {
        val c1 = (CifradoSimetrico.cifrar(texto, contrasena, AlgoritmoSimetrico.CHACHA20) as ResultadoCifrado.Exito).texto
        val c2 = (CifradoSimetrico.cifrar(texto, contrasena, AlgoritmoSimetrico.CHACHA20) as ResultadoCifrado.Exito).texto
        assertNotEquals("Mismo ciphertext indica nonce reutilizado", c1, c2)
    }

    // ── Contraseña incorrecta devuelve Error ──────────────────

    @Test fun aes_contrasenaIncorrecta_devuelveError() {
        val cifrado = (CifradoSimetrico.cifrar(texto, contrasena, AlgoritmoSimetrico.AES) as ResultadoCifrado.Exito).texto
        val resultado = CifradoSimetrico.descifrar(cifrado, "contrasena-erronea", AlgoritmoSimetrico.AES)
        assertTrue(resultado is ResultadoCifrado.Error)
    }

    // ── Validación de entradas vacías ─────────────────────────

    @Test fun cifrar_textoVacio_devuelveError() {
        val resultado = CifradoSimetrico.cifrar("", contrasena, AlgoritmoSimetrico.AES)
        assertTrue(resultado is ResultadoCifrado.Error)
    }

    @Test fun cifrar_contrasenaVacia_devuelveError() {
        val resultado = CifradoSimetrico.cifrar(texto, "", AlgoritmoSimetrico.AES)
        assertTrue(resultado is ResultadoCifrado.Error)
    }

    @Test fun descifrar_textoCifradoVacio_devuelveError() {
        val resultado = CifradoSimetrico.descifrar("", contrasena, AlgoritmoSimetrico.AES)
        assertTrue(resultado is ResultadoCifrado.Error)
    }

    @Test fun descifrar_textoCifradoInvalido_devuelveError() {
        val resultado = CifradoSimetrico.descifrar("esto-no-es-base64-valido!!!", contrasena, AlgoritmoSimetrico.AES)
        assertTrue(resultado is ResultadoCifrado.Error)
    }

    // ── Texto largo ───────────────────────────────────────────

    @Test fun aes_textoLargo_roundtripCorrecto() {
        val textoLargo = "A".repeat(10_000)
        val cifrado    = CifradoSimetrico.cifrar(textoLargo, contrasena, AlgoritmoSimetrico.AES)
        assertTrue(cifrado is ResultadoCifrado.Exito)
        val descifrado = CifradoSimetrico.descifrar((cifrado as ResultadoCifrado.Exito).texto, contrasena, AlgoritmoSimetrico.AES)
        assertEquals(textoLargo, (descifrado as ResultadoCifrado.Exito).texto)
    }

    // ── Caracteres especiales y Unicode ───────────────────────

    @Test fun aes_textoUnicode_roundtripCorrecto() {
        val textoUnicode = "Contraseñas con ñ, ü, 中文, 日本語, 한국어, emojis 🔐🔑"
        val cifrado      = CifradoSimetrico.cifrar(textoUnicode, contrasena, AlgoritmoSimetrico.AES)
        assertTrue(cifrado is ResultadoCifrado.Exito)
        val descifrado   = CifradoSimetrico.descifrar((cifrado as ResultadoCifrado.Exito).texto, contrasena, AlgoritmoSimetrico.AES)
        assertEquals(textoUnicode, (descifrado as ResultadoCifrado.Exito).texto)
    }
}
