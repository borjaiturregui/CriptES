package com.criptes.app.criptografia

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Random

class CifradoArchivosTest {

    private val clave = "contraseña-de-archivo"

    private fun cifrar(datos: ByteArray, pass: String = clave): ByteArray {
        val salida = ByteArrayOutputStream()
        val r = CifradoArchivos.cifrar(ByteArrayInputStream(datos), salida, pass)
        assertTrue("$r", r is ResultadoArchivo.Exito)
        assertEquals(datos.size.toLong(), (r as ResultadoArchivo.Exito).bytesProcesados)
        return salida.toByteArray()
    }

    private fun descifrar(datos: ByteArray, pass: String = clave): Pair<ResultadoArchivo, ByteArray> {
        val salida = ByteArrayOutputStream()
        val r = CifradoArchivos.descifrar(ByteArrayInputStream(datos), salida, pass)
        return r to salida.toByteArray()
    }

    private fun aleatorio(n: Int) = ByteArray(n).also { Random(42).nextBytes(it) }

    @Test fun roundtrip_variosTamaños_incluidosLimitesDeBloque() {
        for (n in listOf(0, 1, 100, 65_535, 65_536, 65_537, 131_072, 200_000)) {
            val original = aleatorio(n)
            val (r, plano) = descifrar(cifrar(original))
            assertTrue("n=$n $r", r is ResultadoArchivo.Exito)
            assertArrayEquals("n=$n", original, plano)
        }
    }

    @Test fun contrasenaIncorrecta_devuelveErrorYNoEscribeDatos() {
        val (r, plano) = descifrar(cifrar(aleatorio(1000)), "otra")
        assertTrue(r is ResultadoArchivo.Error)
        assertEquals(0, plano.size)
    }

    @Test fun cifradosDelMismoArchivo_sonDistintos() {
        val d = aleatorio(500)
        assertFalse(cifrar(d).contentEquals(cifrar(d)))
    }

    @Test fun alterarCualquierParte_devuelveError() {
        val original = cifrar(aleatorio(150_000))
        // magia, versión, sal, prefijo de nonce, longitud de bloque, datos, etiqueta final
        for (pos in listOf(0, 4, 5, 20, 21, 25, 30, original.size / 2, original.size - 1)) {
            val alterado = original.copyOf().also { it[pos] = (it[pos].toInt() xor 1).toByte() }
            assertTrue("pos=$pos", descifrar(alterado).first is ResultadoArchivo.Error)
        }
    }

    @Test fun truncarEnLimiteDeBloque_seDetecta() {
        // 3 bloques completos + uno final: quitar el último bloque entero
        val original = cifrar(aleatorio(65_536 * 3 + 10))
        val ultimoBloque = 4 + 10 + 16
        assertTrue(descifrar(original.copyOf(original.size - ultimoBloque)).first is ResultadoArchivo.Error)
        assertTrue(descifrar(original.copyOf(original.size - 1)).first is ResultadoArchivo.Error)
    }

    @Test fun reordenarBloques_seDetecta() {
        val original = cifrar(aleatorio(65_536 * 2 + 5))
        val inicio = 4 + 1 + 16 + 4
        val tam = 4 + 65_536 + 16
        val b0 = original.copyOfRange(inicio, inicio + tam)
        val b1 = original.copyOfRange(inicio + tam, inicio + 2 * tam)
        val resto = original.copyOfRange(inicio + 2 * tam, original.size)
        val reordenado = original.copyOfRange(0, inicio) + b1 + b0 + resto
        assertTrue(descifrar(reordenado).first is ResultadoArchivo.Error)
    }

    @Test fun datosAnadidosAlFinal_seDetectan() {
        assertTrue(descifrar(cifrar(aleatorio(100)) + byteArrayOf(1, 2, 3)).first is ResultadoArchivo.Error)
    }

    @Test fun archivoNoCriptes_devuelveError() {
        val (r, _) = descifrar("esto no es un archivo cifrado".toByteArray())
        assertTrue(r is ResultadoArchivo.Error)
        assertTrue(descifrar(ByteArray(0)).first is ResultadoArchivo.Error)
    }

    @Test fun contrasenaVacia_devuelveError() {
        assertTrue(CifradoArchivos.cifrar(ByteArrayInputStream(ByteArray(1)), ByteArrayOutputStream(), " ") is ResultadoArchivo.Error)
    }

    @Test fun nombresSugeridos() {
        assertEquals("foto.png.criptes", CifradoArchivos.nombreCifrado("foto.png"))
        assertEquals("foto.png", CifradoArchivos.nombreDescifrado("foto.png.criptes"))
        assertEquals("raro.descifrado", CifradoArchivos.nombreDescifrado("raro"))
        assertEquals(".criptes.descifrado", CifradoArchivos.nombreDescifrado(".criptes"))
    }
}
