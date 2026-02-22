package com.criptes.app.criptografia

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.ByteArrayOutputStream

// ============================================================
//  CriptES — Motor de Esteganografía
//  Técnica LSB (Least Significant Bit) en imágenes PNG/JPG
// ============================================================

/**
 * Resultado de operaciones de esteganografía.
 */
sealed class ResultadoEsteganografia {
    data class ExitoOcultar(val imagenModificada: Bitmap)  : ResultadoEsteganografia()
    data class ExitoExtraer(val mensajeOculto: String)     : ResultadoEsteganografia()
    data class Error(val mensaje: String)                  : ResultadoEsteganografia()
}

/**
 * Motor de esteganografía por técnica LSB (Least Significant Bit).
 *
 * ¿Qué es LSB?
 * Cada píxel de una imagen tiene canales de color (R, G, B),
 * cada uno con un valor de 0 a 255 (8 bits).
 *
 * El bit menos significativo de cada canal tiene un impacto
 * visual mínimo (cambia el color solo en 1/255).
 *
 * Al modificar el LSB de los canales R, G, B de cada píxel,
 * podemos almacenar 3 bits por píxel sin que el ojo humano
 * note ninguna diferencia.
 *
 * Ejemplo:
 *  Color original: R=200 (11001000)
 *  Almacenamos '1': R=201 (11001001)
 *  La diferencia visual es imperceptible.
 */
object Esteganografia {

    // Marcador que señala el fin del mensaje oculto
    private const val MARCADOR_FIN = "<<<CRIPTES_FIN>>>"

    // ── API Pública ──────────────────────────────────────────

    /**
     * Oculta un mensaje de texto dentro de una imagen usando LSB.
     *
     * @param imagen  Bitmap de la imagen portadora (PNG recomendado)
     * @param mensaje Texto a ocultar dentro de la imagen
     * @return ResultadoEsteganografia.ExitoOcultar con el Bitmap modificado
     */
    fun ocultarMensaje(imagen: Bitmap, mensaje: String): ResultadoEsteganografia {
        if (mensaje.isBlank()) return ResultadoEsteganografia.Error("El mensaje no puede estar vacío")

        // Preparar el mensaje con marcador de fin
        val mensajeCompleto = mensaje + MARCADOR_FIN
        val bitsDeTexto     = textoABits(mensajeCompleto)

        // Verificar capacidad de la imagen
        val capacidadBits = imagen.width * imagen.height * 3  // 3 bits por píxel (R, G, B)
        if (bitsDeTexto.size > capacidadBits) {
            val maxCaracteres = (capacidadBits / 8) - MARCADOR_FIN.length
            return ResultadoEsteganografia.Error(
                "Imagen muy pequeña. Capacidad máxima: ~$maxCaracteres caracteres"
            )
        }

        return try {
            // Copiar la imagen para no modificar la original
            val imagenModificada = imagen.copy(Bitmap.Config.ARGB_8888, true)
            var indiceBit        = 0

            // Recorrer cada píxel y modificar los LSBs
            buclePixeles@ for (y in 0 until imagenModificada.height) {
                for (x in 0 until imagenModificada.width) {
                    if (indiceBit >= bitsDeTexto.size) break@buclePixeles

                    val pixel = imagenModificada.getPixel(x, y)
                    var r = Color.red(pixel)
                    var g = Color.green(pixel)
                    var b = Color.blue(pixel)

                    // Modificar canal R (si hay bits disponibles)
                    if (indiceBit < bitsDeTexto.size) {
                        r = modificarLSB(r, bitsDeTexto[indiceBit++])
                    }
                    // Modificar canal G
                    if (indiceBit < bitsDeTexto.size) {
                        g = modificarLSB(g, bitsDeTexto[indiceBit++])
                    }
                    // Modificar canal B
                    if (indiceBit < bitsDeTexto.size) {
                        b = modificarLSB(b, bitsDeTexto[indiceBit++])
                    }

                    imagenModificada.setPixel(x, y, Color.rgb(r, g, b))
                }
            }

            ResultadoEsteganografia.ExitoOcultar(imagenModificada)
        } catch (e: Exception) {
            ResultadoEsteganografia.Error("Error ocultando mensaje: ${e.localizedMessage}")
        }
    }

    /**
     * Extrae un mensaje oculto de una imagen modificada por LSB.
     *
     * @param imagen Bitmap que puede contener un mensaje oculto
     * @return ResultadoEsteganografia.ExitoExtraer con el mensaje, o Error
     */
    fun extraerMensaje(imagen: Bitmap): ResultadoEsteganografia {
        return try {
            val bitsExtraidos = StringBuilder()
            val bytesExtraidos = mutableListOf<Int>()

            buclePixeles@ for (y in 0 until imagen.height) {
                for (x in 0 until imagen.width) {
                    val pixel = imagen.getPixel(x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)

                    // Extraer LSB de cada canal
                    bitsExtraidos.append(r and 1)
                    bitsExtraidos.append(g and 1)
                    bitsExtraidos.append(b and 1)

                    // Cuando tenemos 8 bits, convertir a carácter
                    while (bitsExtraidos.length >= 8) {
                        val byte     = bitsExtraidos.substring(0, 8).toInt(2)
                        bitsExtraidos.delete(0, 8)
                        bytesExtraidos.add(byte)
                    }

                    // Verificar si ya encontramos el marcador de fin
                    val textoActual = String(bytesExtraidos.map { it.toByte() }.toByteArray())
                    if (textoActual.contains(MARCADOR_FIN)) break@buclePixeles
                }
            }

            val textoCompleto = String(bytesExtraidos.map { it.toByte() }.toByteArray())

            if (!textoCompleto.contains(MARCADOR_FIN)) {
                return ResultadoEsteganografia.Error(
                    "No se encontró ningún mensaje oculto en esta imagen.\n" +
                    "Asegúrate de usar una imagen generada por CriptES."
                )
            }

            val mensaje = textoCompleto.substringBefore(MARCADOR_FIN)
            ResultadoEsteganografia.ExitoExtraer(mensaje)

        } catch (e: Exception) {
            ResultadoEsteganografia.Error("Error extrayendo mensaje: ${e.localizedMessage}")
        }
    }

    /**
     * Calcula cuántos caracteres puede almacenar una imagen.
     *
     * @param imagen Bitmap a evaluar
     * @return Número máximo aproximado de caracteres
     */
    fun calcularCapacidad(imagen: Bitmap): Int {
        val capacidadBits = imagen.width * imagen.height * 3
        return (capacidadBits / 8) - MARCADOR_FIN.length - 10 // Margen de seguridad
    }

    // ── Funciones auxiliares ─────────────────────────────────

    /**
     * Convierte un texto a una lista de bits (0s y 1s).
     * Usa codificación UTF-8.
     */
    private fun textoABits(texto: String): List<Int> {
        val bytes = texto.toByteArray(Charsets.UTF_8)
        return bytes.flatMap { byte ->
            (7 downTo 0).map { i -> (byte.toInt() shr i) and 1 }
        }
    }

    /**
     * Modifica el bit menos significativo de un valor de color.
     *
     * @param valorColor Valor del canal (0-255)
     * @param bit        El bit a escribir (0 o 1)
     * @return El nuevo valor del canal con el LSB modificado
     */
    private fun modificarLSB(valorColor: Int, bit: Int): Int {
        return (valorColor and 0xFE) or bit  // Limpiar LSB y escribir el nuevo
    }

    /**
     * Convierte un Bitmap a ByteArray en formato PNG.
     * Útil para guardar la imagen esteganografiada.
     */
    fun bitmapABytes(bitmap: Bitmap): ByteArray {
        val salida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, salida)
        return salida.toByteArray()
    }
}
