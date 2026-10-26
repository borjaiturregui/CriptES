package com.criptes.app.criptografia

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import java.io.ByteArrayOutputStream

// ============================================================
//  CriptES — Motor de Esteganografía
//  Técnica LSB (Least Significant Bit) en imágenes
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
 * Cada píxel tiene canales R, G y B de 8 bits. Cambiar el bit menos
 * significativo de un canal altera el color en 1/255, imperceptible a
 * simple vista, y permite guardar 3 bits por píxel.
 *
 * La lógica de bits vive en [EsteganografiaLSB] (probada en JVM); esta
 * clase solo adapta [Bitmap] a un array de píxeles.
 *
 * - La imagen se aplana sobre fondo negro antes de ocultar: así todos los
 *   píxeles son opacos y los valores RGB no se alteran por la
 *   premultiplicación del alfa al guardar el PNG.
 * - La imagen resultante debe guardarse como PNG (sin pérdida).
 */
object Esteganografia {

    /**
     * Oculta un mensaje de texto dentro de una imagen.
     *
     * @param contrasena Opcional. Si no está vacía, el mensaje se cifra con AES-256-GCM.
     */
    fun ocultarMensaje(imagen: Bitmap, mensaje: String, contrasena: String = ""): ResultadoEsteganografia {
        if (mensaje.isBlank()) return ResultadoEsteganografia.Error("El mensaje no puede estar vacío")

        val contenido = EsteganografiaLSB.empaquetar(mensaje, contrasena).getOrElse {
            return ResultadoEsteganografia.Error("Error preparando el mensaje: ${it.localizedMessage}")
        }
        return try {
            val aplanada = aplanar(imagen)
            val pixeles  = IntArray(aplanada.width * aplanada.height)
            aplanada.getPixels(pixeles, 0, aplanada.width, 0, 0, aplanada.width, aplanada.height)

            if (!EsteganografiaLSB.ocultar(pixeles, contenido)) {
                return ResultadoEsteganografia.Error(
                    "Imagen demasiado pequeña. Capacidad máxima: ~${calcularCapacidad(imagen)} bytes de mensaje " +
                    "(el mensaje ocupa ${contenido.size})"
                )
            }
            aplanada.setPixels(pixeles, 0, aplanada.width, 0, 0, aplanada.width, aplanada.height)
            ResultadoEsteganografia.ExitoOcultar(aplanada)
        } catch (e: Exception) {
            ResultadoEsteganografia.Error("Error ocultando mensaje: ${e.localizedMessage}")
        }
    }

    /**
     * Extrae el mensaje oculto de una imagen generada por CriptES.
     *
     * @param contrasena Necesaria solo si el mensaje se ocultó con contraseña.
     */
    fun extraerMensaje(imagen: Bitmap, contrasena: String = ""): ResultadoEsteganografia {
        return try {
            val aplanada = aplanar(imagen)
            val pixeles  = IntArray(aplanada.width * aplanada.height)
            aplanada.getPixels(pixeles, 0, aplanada.width, 0, 0, aplanada.width, aplanada.height)

            val contenido = EsteganografiaLSB.extraer(pixeles)
                ?: return ResultadoEsteganografia.Error(
                    "No se encontró ningún mensaje oculto en esta imagen.\n" +
                    "Usa un PNG generado por CriptES, sin recomprimir ni redimensionar."
                )
            EsteganografiaLSB.desempaquetar(contenido, contrasena).fold(
                onSuccess = { ResultadoEsteganografia.ExitoExtraer(it) },
                onFailure = { ResultadoEsteganografia.Error(it.localizedMessage ?: "Error extrayendo mensaje") }
            )
        } catch (e: Exception) {
            ResultadoEsteganografia.Error("Error extrayendo mensaje: ${e.localizedMessage}")
        }
    }

    /** Bytes de mensaje (tras el modo y, si hay contraseña, el cifrado) que caben en la imagen. */
    fun calcularCapacidad(imagen: Bitmap): Int =
        maxOf(0, EsteganografiaLSB.capacidadBytes(imagen.width * imagen.height) - 1)

    /** Convierte un Bitmap a PNG (sin pérdida), para guardarlo. */
    fun bitmapABytes(bitmap: Bitmap): ByteArray {
        val salida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, salida)
        return salida.toByteArray()
    }

    /** Copia mutable y opaca: la imagen se dibuja sobre fondo negro. */
    private fun aplanar(origen: Bitmap): Bitmap {
        val copia = Bitmap.createBitmap(origen.width, origen.height, Bitmap.Config.ARGB_8888)
        Canvas(copia).apply {
            drawColor(Color.BLACK)
            drawBitmap(origen, 0f, 0f, null)
        }
        return copia
    }
}
