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
