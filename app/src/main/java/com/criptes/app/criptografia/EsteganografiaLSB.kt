package com.criptes.app.criptografia

// ============================================================
//  CriptES — Núcleo LSB (sin dependencias de Android)
//  Trabaja sobre arrays de píxeles ARGB para poder probarse en JVM
// ============================================================

/**
 * Oculta y extrae bytes en los bits menos significativos (LSB) de los
 * canales R, G y B de una imagen (3 bits por píxel, el alfa no se toca).
 *
 * Formato de la carga oculta:
 *   "CRPS" (4) | longitud del contenido (4, big-endian) | contenido
 *
 * Contenido (lo construye [empaquetar]):
 *   modo (1 byte: 0 = texto plano, 1 = cifrado con contraseña) | texto UTF-8
 *   (o, en modo 1, el Base64 que produce [CifradoSimetrico] con AES-256-GCM)
 *
 * LSB no es cifrado: sin contraseña el mensaje se lee con cualquier extractor.
 * Solo sobrevive en formatos sin pérdida (PNG); una recompresión JPEG o un
 * redimensionado lo destruyen.
 */
object EsteganografiaLSB {

    private val MAGIA = byteArrayOf('C'.code.toByte(), 'R'.code.toByte(), 'P'.code.toByte(), 'S'.code.toByte())
    private const val CABECERA = 8

    const val MODO_PLANO   = 0
    const val MODO_CIFRADO = 1

    /** Bytes de contenido que caben en una imagen de [numeroPixeles] píxeles. */
    fun capacidadBytes(numeroPixeles: Int): Int = maxOf(0, (numeroPixeles.toLong() * 3 / 8).toInt() - CABECERA)

    /** Escribe [contenido] en los píxeles (in situ). Devuelve false si no cabe. */
    fun ocultar(pixeles: IntArray, contenido: ByteArray): Boolean {
        if (contenido.size > capacidadBytes(pixeles.size)) return false
        val carga = MAGIA + enteroABytes(contenido.size) + contenido
        var bit = 0
        val totalBits = carga.size * 8
        var i = 0
        while (bit < totalBits) {
            var p = pixeles[i]
            for (desplazamiento in intArrayOf(16, 8, 0)) {          // R, G, B
                if (bit >= totalBits) break
                val valor = (carga[bit / 8].toInt() shr (7 - bit % 8)) and 1
                p = (p and (1 shl desplazamiento).inv()) or (valor shl desplazamiento)
                bit++
            }
            pixeles[i++] = p
        }
        return true
    }

    /** Lee el contenido oculto, o null si la imagen no contiene una carga válida. */
    fun extraer(pixeles: IntArray): ByteArray? {
        if (pixeles.size * 3 < CABECERA * 8) return null
        val cabecera = leerBytes(pixeles, 0, CABECERA)
        if (!cabecera.copyOfRange(0, 4).contentEquals(MAGIA)) return null
        val longitud = bytesAEntero(cabecera, 4)
        if (longitud < 0 || longitud > capacidadBytes(pixeles.size)) return null
        return leerBytes(pixeles, CABECERA, longitud)
    }

    // ── Mensaje de texto ↔ contenido ─────────────────────────

    /** Construye el contenido a ocultar. Con contraseña, el texto se cifra antes. */
    fun empaquetar(mensaje: String, contrasena: String): Result<ByteArray> {
        if (contrasena.isBlank()) return Result.success(byteArrayOf(MODO_PLANO.toByte()) + mensaje.toByteArray(Charsets.UTF_8))
        return when (val r = CifradoSimetrico.cifrar(mensaje, contrasena, AlgoritmoSimetrico.AES)) {
            is ResultadoCifrado.Exito -> Result.success(byteArrayOf(MODO_CIFRADO.toByte()) + r.texto.toByteArray(Charsets.UTF_8))
            is ResultadoCifrado.Error -> Result.failure(IllegalStateException(r.mensaje))
        }
    }

    /** Inversa de [empaquetar]. Los fallos se devuelven como mensaje para la UI. */
    fun desempaquetar(contenido: ByteArray, contrasena: String): Result<String> {
        if (contenido.isEmpty()) return Result.failure(IllegalStateException("Contenido vacío"))
        val cuerpo = String(contenido, 1, contenido.size - 1, Charsets.UTF_8)
        return when (contenido[0].toInt()) {
            MODO_PLANO -> Result.success(cuerpo)
            MODO_CIFRADO -> {
                if (contrasena.isBlank()) return Result.failure(IllegalStateException("Este mensaje está protegido: introduce la contraseña"))
                when (val r = CifradoSimetrico.descifrar(cuerpo, contrasena, AlgoritmoSimetrico.AES)) {
                    is ResultadoCifrado.Exito -> Result.success(r.texto)
                    is ResultadoCifrado.Error -> Result.failure(IllegalStateException("Contraseña incorrecta o imagen alterada"))
                }
            }
            else -> Result.failure(IllegalStateException("Formato de mensaje desconocido"))
        }
    }

    // ── Auxiliares ───────────────────────────────────────────

    private fun leerBytes(pixeles: IntArray, desdeByte: Int, cantidad: Int): ByteArray {
        val salida = ByteArray(cantidad)
        for (n in 0 until cantidad * 8) {
            val bitGlobal = desdeByte * 8 + n
            val pixel = pixeles[bitGlobal / 3]
            val desplazamiento = 16 - 8 * (bitGlobal % 3)
            val valor = (pixel shr desplazamiento) and 1
            salida[n / 8] = (salida[n / 8].toInt() or (valor shl (7 - n % 8))).toByte()
        }
        return salida
    }

    private fun enteroABytes(v: Int) = byteArrayOf((v ushr 24).toByte(), (v ushr 16).toByte(), (v ushr 8).toByte(), v.toByte())

    private fun bytesAEntero(b: ByteArray, desde: Int) =
        ((b[desde].toInt() and 0xFF) shl 24) or ((b[desde + 1].toInt() and 0xFF) shl 16) or
        ((b[desde + 2].toInt() and 0xFF) shl 8) or (b[desde + 3].toInt() and 0xFF)
}
