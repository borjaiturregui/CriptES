package com.criptes.app.criptografia

import org.junit.Assert.*
import org.junit.Test

class GeneradorHashTest {

    // ── Vectores de prueba conocidos (NIST / RFC) ─────────────

    @Test fun md5_abc_devuelveHashConocido() {
        val resultado = GeneradorHash.generar("abc", AlgoritmoHash.MD5)
        assertTrue(resultado is ResultadoHash.Exito)
        assertEquals("900150983cd24fb0d6963f7d28e17f72", (resultado as ResultadoHash.Exito).hash)
    }

    @Test fun sha256_abc_devuelveHashConocido() {
        val resultado = GeneradorHash.generar("abc", AlgoritmoHash.SHA256)
        assertTrue(resultado is ResultadoHash.Exito)
        assertEquals("ba7816bf8f01cfea414140de5dae2ec73b00361bbef0469f490f67457112b3f7", (resultado as ResultadoHash.Exito).hash)
    }

    @Test fun sha512_abc_devuelveHashConocido() {
        val resultado = GeneradorHash.generar("abc", AlgoritmoHash.SHA512)
        assertTrue(resultado is ResultadoHash.Exito)
        assertEquals(
            "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f",
            (resultado as ResultadoHash.Exito).hash
        )
    }

    // ── Texto vacío devuelve Error ────────────────────────────

    @Test fun generar_textoVacio_devuelveError() {
        val resultado = GeneradorHash.generar("", AlgoritmoHash.SHA256)
        assertTrue(resultado is ResultadoHash.Error)
    }

    // ── Determinismo: mismo input = mismo hash ────────────────

    @Test fun sha256_mismoInput_siempreDevuelveMismoHash() {
        val r1 = GeneradorHash.generar("texto de prueba", AlgoritmoHash.SHA256) as ResultadoHash.Exito
        val r2 = GeneradorHash.generar("texto de prueba", AlgoritmoHash.SHA256) as ResultadoHash.Exito
        assertEquals(r1.hash, r2.hash)
    }

    // ── Efecto avalancha: pequeño cambio altera todo ──────────

    @Test fun sha256_unCaracterDiferente_producirHashCompletamenteDiferente() {
        val h1 = (GeneradorHash.generar("abc", AlgoritmoHash.SHA256) as ResultadoHash.Exito).hash
        val h2 = (GeneradorHash.generar("abd", AlgoritmoHash.SHA256) as ResultadoHash.Exito).hash
        assertNotEquals(h1, h2)
    }

    // ── Longitudes correctas ──────────────────────────────────

    @Test fun md5_longitudCorrecta() {
        val hash = (GeneradorHash.generar("test", AlgoritmoHash.MD5) as ResultadoHash.Exito).hash
        assertEquals(32, hash.length)   // 128 bits = 32 hex chars
    }

    @Test fun sha1_longitudCorrecta() {
        val hash = (GeneradorHash.generar("test", AlgoritmoHash.SHA1) as ResultadoHash.Exito).hash
        assertEquals(40, hash.length)   // 160 bits = 40 hex chars
    }

    @Test fun sha256_longitudCorrecta() {
        val hash = (GeneradorHash.generar("test", AlgoritmoHash.SHA256) as ResultadoHash.Exito).hash
        assertEquals(64, hash.length)   // 256 bits = 64 hex chars
    }

    @Test fun sha512_longitudCorrecta() {
        val hash = (GeneradorHash.generar("test", AlgoritmoHash.SHA512) as ResultadoHash.Exito).hash
        assertEquals(128, hash.length)  // 512 bits = 128 hex chars
    }

    // ── generarTodos devuelve los 4 algoritmos ────────────────

    @Test fun generarTodos_devuelveCuatroEntradas() {
        val todos = GeneradorHash.generarTodos("hola")
        assertEquals(4, todos.size)
        assertTrue(todos.containsKey(AlgoritmoHash.MD5))
        assertTrue(todos.containsKey(AlgoritmoHash.SHA1))
        assertTrue(todos.containsKey(AlgoritmoHash.SHA256))
        assertTrue(todos.containsKey(AlgoritmoHash.SHA512))
    }

    // ── Solo minúsculas hexadecimales ─────────────────────────

    @Test fun sha256_resultadoEsHexadecimalMinusculas() {
        val hash = (GeneradorHash.generar("test", AlgoritmoHash.SHA256) as ResultadoHash.Exito).hash
        assertTrue("El hash debe contener solo hex minúsculas", hash.matches(Regex("[0-9a-f]+")))
    }

    // ── longitudHex coincide con la longitud real ─────────────

    @Test fun exito_longitudHexCoincidesConLongitudHash() {
        val resultado = GeneradorHash.generar("criptes", AlgoritmoHash.SHA256) as ResultadoHash.Exito
        assertEquals(resultado.longitudHex, resultado.hash.length)
    }
}
