package com.criptes.app.criptografia

import org.junit.Assert.*
import org.junit.Test
import java.util.Random

class EsteganografiaLSBTest {

    private fun imagen(n: Int, semilla: Long = 7) = IntArray(n).also { px ->
        val r = Random(semilla); for (i in px.indices) px[i] = (0xFF shl 24) or r.nextInt(0x1000000)
    }

    @Test fun roundtrip_bytes() {
        val px = imagen(1000)
        val dato = "hola mundo ñ 🔐".toByteArray()
        assertTrue(EsteganografiaLSB.ocultar(px, dato))
        assertArrayEquals(dato, EsteganografiaLSB.extraer(px))
    }

    @Test fun solo_cambianLosLSBdeRGB_yNoElAlfa() {
        val original = imagen(500)
        val px = original.copyOf()
        EsteganografiaLSB.ocultar(px, ByteArray(100) { 0x55 })
        for (i in px.indices) {
            assertEquals(original[i] ushr 24, px[i] ushr 24)             // alfa intacto
            assertEquals(0, (original[i] xor px[i]) and 0xFEFEFE.toInt() and 0xFFFFFF) // solo bit 0 de R,G,B
        }
    }

    @Test fun imagenSinMensaje_devuelveNull() {
        assertNull(EsteganografiaLSB.extraer(imagen(1000)))
        assertNull(EsteganografiaLSB.extraer(IntArray(0)))
        assertNull(EsteganografiaLSB.extraer(IntArray(10)))
    }

    @Test fun capacidad_exacta() {
        val n = 800                                  // 2400 bits = 300 bytes - 8 de cabecera
        assertEquals(292, EsteganografiaLSB.capacidadBytes(n))
        assertTrue(EsteganografiaLSB.ocultar(imagen(n), ByteArray(292) { 1 }))
        assertFalse(EsteganografiaLSB.ocultar(imagen(n), ByteArray(293)))
        val px = imagen(n); EsteganografiaLSB.ocultar(px, ByteArray(292) { 9 })
        assertArrayEquals(ByteArray(292) { 9 }, EsteganografiaLSB.extraer(px))
    }

    @Test fun cabeceraCorrupta_noDesbordaYDevuelveNull() {
        val px = imagen(1000); EsteganografiaLSB.ocultar(px, ByteArray(10))
        px[20] = px[20] xor 1; px[21] = px[21] xor 1 // altera bits de la longitud
        val r = EsteganografiaLSB.extraer(px)
        assertTrue(r == null || r.size <= EsteganografiaLSB.capacidadBytes(1000))
    }

    @Test fun mensajeSinContrasena_roundtrip() {
        val px = imagen(5000)
        val c = EsteganografiaLSB.empaquetar("secreto ñ", "").getOrThrow()
        assertTrue(EsteganografiaLSB.ocultar(px, c))
        assertEquals("secreto ñ", EsteganografiaLSB.desempaquetar(EsteganografiaLSB.extraer(px)!!, "").getOrThrow())
    }

    @Test fun mensajeConContrasena_roundtripYRechazos() {
        val px = imagen(20000)
        val c = EsteganografiaLSB.empaquetar("muy secreto", "clave").getOrThrow()
        assertTrue(EsteganografiaLSB.ocultar(px, c))
        val extraido = EsteganografiaLSB.extraer(px)!!
        assertEquals("muy secreto", EsteganografiaLSB.desempaquetar(extraido, "clave").getOrThrow())
        assertTrue(EsteganografiaLSB.desempaquetar(extraido, "otra").isFailure)
        assertTrue(EsteganografiaLSB.desempaquetar(extraido, "").isFailure)
        // el texto plano no debe aparecer en el contenido ocultado
        assertFalse(String(extraido, Charsets.ISO_8859_1).contains("muy secreto"))
    }

    @Test fun contenidoVacioOModoDesconocido_fallaControlado() {
        assertTrue(EsteganografiaLSB.desempaquetar(ByteArray(0), "").isFailure)
        assertTrue(EsteganografiaLSB.desempaquetar(byteArrayOf(9, 1, 2), "").isFailure)
    }
}
