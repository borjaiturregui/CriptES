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

    // ── Integridad: los cifrados autenticados rechazan toda manipulación ──

    private val autenticados = listOf(AlgoritmoSimetrico.AES, AlgoritmoSimetrico.CHACHA20)
    private val tamañoSal = 16

    private fun cifrarOk(algoritmo: AlgoritmoSimetrico, entrada: String = texto): ByteArray =
        java.util.Base64.getDecoder().decode(
            (CifradoSimetrico.cifrar(entrada, contrasena, algoritmo) as ResultadoCifrado.Exito).texto
        )

    private fun descifrarBytes(bytes: ByteArray, algoritmo: AlgoritmoSimetrico, clave: String = contrasena) =
        CifradoSimetrico.descifrar(java.util.Base64.getEncoder().encodeToString(bytes), clave, algoritmo)

    @Test fun autenticados_contrasenaIncorrecta_devuelveError() {
        for (algoritmo in autenticados) {
            val bytes = cifrarOk(algoritmo)
            assertTrue("$algoritmo", descifrarBytes(bytes, algoritmo, "otra-contrasena") is ResultadoCifrado.Error)
        }
    }

    @Test fun autenticados_alterarCualquierByte_devuelveError() {
        for (algoritmo in autenticados) {
            val original = cifrarOk(algoritmo)
            // sal, IV/nonce, datos cifrados y etiqueta (último byte)
            val posiciones = listOf(0, tamañoSal - 1, tamañoSal, tamañoSal + algoritmo.tamañoIV - 1,
                tamañoSal + algoritmo.tamañoIV, original.size - 1)
            for (pos in posiciones) {
                val alterado = original.copyOf().also { it[pos] = (it[pos].toInt() xor 0x01).toByte() }
                assertTrue("$algoritmo byte $pos", descifrarBytes(alterado, algoritmo) is ResultadoCifrado.Error)
            }
        }
    }

    @Test fun autenticados_truncarOAnadirBytes_devuelveError() {
        for (algoritmo in autenticados) {
            val original = cifrarOk(algoritmo)
            assertTrue(descifrarBytes(original.copyOf(original.size - 1), algoritmo) is ResultadoCifrado.Error)
            assertTrue(descifrarBytes(original + byteArrayOf(0), algoritmo) is ResultadoCifrado.Error)
        }
    }

    @Test fun todos_entradaMasCortaQueSalMasIV_devuelveErrorControlado() {
        for (algoritmo in AlgoritmoSimetrico.values()) {
            for (tamaño in listOf(1, tamañoSal, tamañoSal + algoritmo.tamañoIV)) {
                assertTrue("$algoritmo $tamaño", descifrarBytes(ByteArray(tamaño), algoritmo) is ResultadoCifrado.Error)
            }
        }
    }

    @Test fun todos_base64Invalido_devuelveErrorControlado() {
        for (algoritmo in AlgoritmoSimetrico.values()) {
            assertTrue(CifradoSimetrico.descifrar("%%%no-base64%%%", contrasena, algoritmo) is ResultadoCifrado.Error)
        }
    }

    @Test fun todos_cifradoDeUnAlgoritmoNoSeDescifraConOtro() {
        for (a in AlgoritmoSimetrico.values()) for (b in AlgoritmoSimetrico.values()) {
            if (a == b || !(a.autenticado || b.autenticado)) continue
            val cifrado = (CifradoSimetrico.cifrar(texto, contrasena, a) as ResultadoCifrado.Exito).texto
            val r = CifradoSimetrico.descifrar(cifrado, contrasena, b)
            assertFalse("$a → $b", r is ResultadoCifrado.Exito && r.texto == texto)
        }
    }

    // ── DES/3DES: sin autenticación (se documenta el comportamiento real) ──

    @Test fun desYTripleDes_contrasenaIncorrecta_nuncaDevuelveElTextoOriginal() {
        // CBC no autentica: con otra clave puede dar Error (relleno inválido)
        // o basura; lo que nunca debe dar es el texto original.
        for (algoritmo in listOf(AlgoritmoSimetrico.DES, AlgoritmoSimetrico.TRIPLE_DES)) {
            val bytes = cifrarOk(algoritmo)
            val r = descifrarBytes(bytes, algoritmo, "otra-contrasena")
            assertFalse("$algoritmo", r is ResultadoCifrado.Exito && r.texto == texto)
        }
    }

    @Test fun enum_marcaAutenticadosYObsoletosCorrectamente() {
        assertTrue(AlgoritmoSimetrico.AES.autenticado)
        assertTrue(AlgoritmoSimetrico.CHACHA20.autenticado)
        assertFalse(AlgoritmoSimetrico.DES.autenticado)
        assertFalse(AlgoritmoSimetrico.TRIPLE_DES.autenticado)
    }

    // ── Aleatoriedad por operación ────────────────────────────

    @Test fun todos_salEIVNoSeReutilizan() {
        for (algoritmo in AlgoritmoSimetrico.values()) {
            val salesEIVs = (1..20).map { cifrarOk(algoritmo).copyOfRange(0, tamañoSal + algoritmo.tamañoIV).toList() }
            assertEquals("$algoritmo", 20, salesEIVs.toSet().size)
        }
    }

    // ── Todos los algoritmos: roundtrip Unicode y largo ──────

    @Test fun todos_roundtripUnicodeYLargo() {
        val unicode = "ñ ü 中文 日本語 🔐 " + "x".repeat(5_000)
        for (algoritmo in AlgoritmoSimetrico.values()) {
            val r = descifrarBytes(cifrarOk(algoritmo, unicode), algoritmo)
            assertEquals("$algoritmo", unicode, (r as ResultadoCifrado.Exito).texto)
        }
    }
}
