package com.criptes.app.criptografia

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.security.Security

/**
 * Pruebas en dispositivo/emulador. Ejecutar en API 26, 27, 28 y una
 * versión reciente, y también contra el APK `release` minificado
 * (p. ej. con `testBuildType = "release"` y reglas de R8 activas).
 */
@RunWith(AndroidJUnit4::class)
class CriptografiaInstrumentadaTest {

    private val etiqueta = "CriptESCripto"

    @Test fun registraProveedoresDisponibles() {
        Security.getProviders().forEach {
            Log.i(etiqueta, "Proveedor ${it.name} v${it.version} (${it.javaClass.name})")
        }
        // CriptES ya no registra ni exige un proveedor "BC" propio.
        assertTrue(Security.getProviders().isNotEmpty())
    }

    @Test fun cadaAlgoritmoSimetrico_roundtripEnDispositivo() {
        for (algoritmo in AlgoritmoSimetrico.values()) {
            val cifrado = CifradoSimetrico.cifrar("Hola CriptES ñ 🔐", "clave-de-prueba", algoritmo)
            assertTrue("$algoritmo cifrar: $cifrado", cifrado is ResultadoCifrado.Exito)
            val plano = CifradoSimetrico.descifrar((cifrado as ResultadoCifrado.Exito).texto, "clave-de-prueba", algoritmo)
            assertEquals("$algoritmo", "Hola CriptES ñ 🔐", (plano as ResultadoCifrado.Exito).texto)
        }
    }

    @Test fun algoritmosAutenticados_rechazanManipulacionEnDispositivo() {
        for (algoritmo in AlgoritmoSimetrico.values().filter { it.autenticado }) {
            val texto = (CifradoSimetrico.cifrar("secreto", "clave", algoritmo) as ResultadoCifrado.Exito).texto
            val bytes = java.util.Base64.getDecoder().decode(texto)
            bytes[bytes.size - 1] = (bytes[bytes.size - 1].toInt() xor 1).toByte()
            val r = CifradoSimetrico.descifrar(java.util.Base64.getEncoder().encodeToString(bytes), "clave", algoritmo)
            assertTrue("$algoritmo", r is ResultadoCifrado.Error)
        }
    }

    @Test fun rsa_generacionYRoundtripEnDispositivo() {
        val claves = (CifradoRSA.generarParClaves() as ResultadoRSA.ExitoClaves).parClaves
        val cifrado = CifradoRSA.cifrar("Hola RSA", claves.clavePublicaPEM)
        assertTrue("$cifrado", cifrado is ResultadoRSA.ExitoCifrado)
        val plano = CifradoRSA.descifrar((cifrado as ResultadoRSA.ExitoCifrado).textoCifrado, claves.clavePrivadaPEM)
        assertEquals("Hola RSA", (plano as ResultadoRSA.ExitoDescifrado).textoPlano)
    }

    @Test fun hashes_vectoresConocidosEnDispositivo() {
        val r = GeneradorHash.generar("abc", AlgoritmoHash.SHA256)
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", (r as ResultadoHash.Exito).hash)
    }
}

/**
 * Pruebas de módulos que dependen de Android (Bitmap) o de flujos de archivo.
 */
@RunWith(AndroidJUnit4::class)
class ModulosInstrumentadosTest {

    private fun bitmapDePrueba(): android.graphics.Bitmap {
        val b = android.graphics.Bitmap.createBitmap(120, 120, android.graphics.Bitmap.Config.ARGB_8888)
        val r = java.util.Random(1)
        for (x in 0 until 120) for (y in 0 until 120) b.setPixel(x, y, android.graphics.Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256)))
        return b
    }

    private fun comoPng(b: android.graphics.Bitmap): android.graphics.Bitmap {
        val bytes = Esteganografia.bitmapABytes(b)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @Test fun esteganografia_roundtripTrasGuardarComoPng() {
        val r = Esteganografia.ocultarMensaje(bitmapDePrueba(), "Mensaje secreto ñ 🔐")
        assertTrue("$r", r is ResultadoEsteganografia.ExitoOcultar)
        val recargada = comoPng((r as ResultadoEsteganografia.ExitoOcultar).imagenModificada)
        val extraido = Esteganografia.extraerMensaje(recargada)
        assertEquals("Mensaje secreto ñ 🔐", (extraido as ResultadoEsteganografia.ExitoExtraer).mensajeOculto)
    }

    @Test fun esteganografia_conContrasena() {
        val r = Esteganografia.ocultarMensaje(bitmapDePrueba(), "privado", "clave") as ResultadoEsteganografia.ExitoOcultar
        val recargada = comoPng(r.imagenModificada)
        assertTrue(Esteganografia.extraerMensaje(recargada, "mala") is ResultadoEsteganografia.Error)
        assertTrue(Esteganografia.extraerMensaje(recargada) is ResultadoEsteganografia.Error)
        assertEquals("privado", (Esteganografia.extraerMensaje(recargada, "clave") as ResultadoEsteganografia.ExitoExtraer).mensajeOculto)
    }

    @Test fun esteganografia_imagenSinMensajeYImagenDemasiadoPequena() {
        assertTrue(Esteganografia.extraerMensaje(bitmapDePrueba()) is ResultadoEsteganografia.Error)
        val minuscula = android.graphics.Bitmap.createBitmap(4, 4, android.graphics.Bitmap.Config.ARGB_8888)
        assertTrue(Esteganografia.ocultarMensaje(minuscula, "x".repeat(100)) is ResultadoEsteganografia.Error)
    }

    @Test fun cifradoArchivos_roundtripEnDispositivo() {
        val original = ByteArray(200_000) { (it * 31).toByte() }
        val cifrado = java.io.ByteArrayOutputStream()
        assertTrue(CifradoArchivos.cifrar(java.io.ByteArrayInputStream(original), cifrado, "clave") is ResultadoArchivo.Exito)
        val plano = java.io.ByteArrayOutputStream()
        assertTrue(CifradoArchivos.descifrar(java.io.ByteArrayInputStream(cifrado.toByteArray()), plano, "clave") is ResultadoArchivo.Exito)
        assertArrayEquals(original, plano.toByteArray())
    }

    @Test fun generadorContrasenas_enDispositivo() {
        val r = GeneradorContrasenas.generar(OpcionesContrasena(longitud = 20))
        assertEquals(20, (r as ResultadoContrasena.Exito).contrasena.length)
    }
}
