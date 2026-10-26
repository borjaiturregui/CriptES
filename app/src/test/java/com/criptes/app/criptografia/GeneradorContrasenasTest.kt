package com.criptes.app.criptografia

import org.junit.Assert.*
import org.junit.Test

class GeneradorContrasenasTest {

    private fun generar(o: OpcionesContrasena) = (GeneradorContrasenas.generar(o) as ResultadoContrasena.Exito)

    @Test fun respetaLaLongitud() {
        for (n in listOf(8, 16, 33, 64)) assertEquals(n, generar(OpcionesContrasena(longitud = n)).contrasena.length)
    }

    @Test fun incluyeAlMenosUnCaracterDeCadaTipoActivado() {
        repeat(300) {
            val c = generar(OpcionesContrasena(longitud = 8)).contrasena
            assertTrue(c, c.any { it.isLowerCase() } && c.any { it.isUpperCase() } &&
                c.any { it.isDigit() } && c.any { !it.isLetterOrDigit() })
        }
    }

    @Test fun soloUsaLosTiposActivados() {
        repeat(100) {
            val c = generar(OpcionesContrasena(minusculas = false, mayusculas = false, simbolos = false)).contrasena
            assertTrue(c, c.all { it.isDigit() })
        }
    }

    @Test fun excluirAmbiguos_noGeneraCaracteresConfusos() {
        repeat(200) {
            val c = generar(OpcionesContrasena(longitud = 64, excluirAmbiguos = true)).contrasena
            assertTrue(c, c.none { it in "0Oo1lI|" })
        }
    }

    @Test fun dosContrasenasSonDistintas() {
        assertNotEquals(generar(OpcionesContrasena()).contrasena, generar(OpcionesContrasena()).contrasena)
    }

    @Test fun distribucionRazonable_sinSesgoGrosero() {
        val o = OpcionesContrasena(longitud = 64, mayusculas = false, numeros = false, simbolos = false)
        val cuentas = IntArray(26)
        repeat(2000) { generar(o).contrasena.forEach { cuentas[it - 'a']++ } }
        val esperado = 2000 * 64 / 26.0
        cuentas.forEach { assertTrue("$it vs $esperado", it > esperado * 0.9 && it < esperado * 1.1) }
    }

    @Test fun entropiaYFuerza() {
        assertEquals(16 * Math.log(94.0) / Math.log(2.0), GeneradorContrasenas.entropiaBits(16, 94), 1e-9)
        assertEquals(FuerzaContrasena.DEBIL, GeneradorContrasenas.clasificar(30.0))
        assertEquals(FuerzaContrasena.ACEPTABLE, GeneradorContrasenas.clasificar(60.0))
        assertEquals(FuerzaContrasena.FUERTE, GeneradorContrasenas.clasificar(80.0))
        assertEquals(FuerzaContrasena.MUY_FUERTE, GeneradorContrasenas.clasificar(120.0))
        assertEquals(FuerzaContrasena.MUY_FUERTE, generar(OpcionesContrasena(longitud = 24)).fuerza)
    }

    @Test fun opcionesInvalidas_devuelvenError() {
        assertTrue(GeneradorContrasenas.generar(OpcionesContrasena(longitud = 7)) is ResultadoContrasena.Error)
        assertTrue(GeneradorContrasenas.generar(OpcionesContrasena(longitud = 65)) is ResultadoContrasena.Error)
        assertTrue(GeneradorContrasenas.generar(
            OpcionesContrasena(minusculas = false, mayusculas = false, numeros = false, simbolos = false)
        ) is ResultadoContrasena.Error)
    }
}
