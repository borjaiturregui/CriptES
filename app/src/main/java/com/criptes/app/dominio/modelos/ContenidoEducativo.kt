package com.criptes.app.dominio.modelos

// ============================================================
//  CriptES — Contenido Educativo
//  Explicaciones en español de cada algoritmo criptográfico
// ============================================================

/**
 * Modelo que contiene toda la información educativa de un algoritmo.
 */
data class InfoEducativa(
    val nombre:           String,
    val categoria:        CategoriaAlgoritmo,
    val historia:         String,
    val comoFunciona:     String,
    val fortalezas:       List<String>,
    val debilidades:      List<String>,
    val casosDeUso:       List<String>,
    val nivelSeguridad:   NivelSeguridad,
    val añoCreacion:      Int,
    val creadores:        String,
    val datoCurioso:      String
)

enum class CategoriaAlgoritmo(val etiqueta: String) {
    SIMETRICO("Cifrado Simétrico"),
    ASIMETRICO("Cifrado Asimétrico"),
    HASH("Función Hash"),
    ESTEGANOGRAFIA("Esteganografía"),
    UTILIDAD("Herramienta de seguridad")
}

enum class NivelSeguridad(val etiqueta: String, val descripcion: String) {
    ROTO("Roto", "No usar para seguridad"),
    DEBIL("Débil", "Vulnerable a ataques modernos"),
    MODERADO("Moderado", "Aceptable en contextos no críticos"),
    FUERTE("Fuerte", "Recomendado para uso actual"),
    MUY_FUERTE("Muy Fuerte", "Estándar de máxima seguridad")
}

/**
 * Repositorio central del contenido educativo de CriptES.
 * Cada algoritmo incluye su historia, funcionamiento y recomendaciones.
 */
object ContenidoEducativo {

    val algoritmos: Map<String, InfoEducativa> = mapOf(

        // ── ChaCha20-Poly1305 ───────────────────────────────
        "CHACHA20" to InfoEducativa(
            nombre       = "ChaCha20-Poly1305",
            categoria    = CategoriaAlgoritmo.SIMETRICO,
            añoCreacion  = 2008,
            creadores    = "Daniel J. Bernstein (ChaCha20) · Poly1305, también de Bernstein",
            nivelSeguridad = NivelSeguridad.MUY_FUERTE,
            historia = """
                ChaCha20 es una variante del cifrador de flujo Salsa20, que Daniel J. Bernstein
                presentó en 2008. Poly1305 es un autenticador de mensajes, también suyo.

                En 2015 se estandarizó su combinación (RFC 7539, hoy RFC 8439) y Google la
                empezó a usar en Chrome y Android porque va muy rápida en móviles sin
                aceleración de hardware para AES. TLS 1.3 la incluye como suite obligatoria
                alternativa a AES-GCM.
            """.trimIndent(),
            comoFunciona = """
                ChaCha20 genera un flujo de bytes pseudoaleatorio a partir de la clave (256 bits)
                y un nonce de 96 bits, y lo suma (XOR) con el mensaje.

                Poly1305 calcula una etiqueta de 16 bytes sobre el texto cifrado. Al descifrar
                se comprueba la etiqueta ANTES de entregar nada: si alguien modificó un solo bit,
                o la contraseña es incorrecta, el descifrado falla.

                En CriptES la clave sale de tu contraseña con PBKDF2-HMAC-SHA-256 y una sal
                aleatoria; el nonce es aleatorio en cada cifrado.
            """.trimIndent(),
            fortalezas = listOf(
                "Cifrado autenticado: detecta cualquier manipulación",
                "Muy rápido en software, incluso en móviles antiguos",
                "Diseño simple, sin tablas que den fugas por tiempos de acceso"
            ),
            debilidades = listOf(
                "Nunca se debe repetir un nonce con la misma clave",
                "Una contraseña débil sigue siendo el eslabón más flojo"
            ),
            casosDeUso = listOf("TLS 1.3 y HTTPS", "WireGuard y otras VPN", "SSH", "Mensajería segura"),
            datoCurioso = """
                El nombre "ChaCha" es un juego con "Salsa": ambos son bailes. La ronda de mezcla
                de ChaCha difunde mejor los bits que la de Salsa con el mismo coste.
            """.trimIndent()
        ),

        // ── Generador de contraseñas ────────────────────────
        "CONTRASENAS" to InfoEducativa(
            nombre       = "Contraseñas aleatorias y entropía",
            categoria    = CategoriaAlgoritmo.UTILIDAD,
            añoCreacion  = 1961,
            creadores    = "Las contraseñas informáticas nacen con el sistema CTSS del MIT (1961)",
            nivelSeguridad = NivelSeguridad.FUERTE,
            historia = """
                La primera contraseña informática conocida es de 1961, en el sistema
                compartido CTSS del MIT. Poco después alguien consiguió imprimir el archivo
                de contraseñas de todos los usuarios: el primer robo de credenciales.

                Desde entonces el problema es el mismo: las personas elegimos contraseñas
                predecibles, y un atacante las prueba por orden de probabilidad.
            """.trimIndent(),
            comoFunciona = """
                La seguridad de una contraseña aleatoria se mide en bits de entropía:
                longitud × log2(tamaño del alfabeto).

                Ejemplo: 16 caracteres de un alfabeto de 94 símbolos ≈ 105 bits. Cada bit
                extra duplica el trabajo de un atacante.

                Para generarla, CriptES usa SecureRandom del sistema, elige cada carácter de
                forma uniforme (sin sesgo), garantiza un carácter de cada tipo activado y
                baraja el resultado.
            """.trimIndent(),
            fortalezas = listOf(
                "Imposible de adivinar por patrones o diccionarios",
                "La entropía mostrada es real porque la contraseña es aleatoria",
                "Se genera y queda en tu dispositivo"
            ),
            debilidades = listOf(
                "Difícil de recordar: conviene un gestor de contraseñas",
                "No sirve de nada si la reutilizas en varios sitios",
                "La entropía solo es válida para contraseñas aleatorias, no para frases elegidas por ti"
            ),
            casosDeUso = listOf("Cuentas nuevas", "Contraseña maestra de un gestor", "Claves de cifrado de archivos"),
            datoCurioso = """
                Una contraseña aleatoria de 12 caracteres con mayúsculas, minúsculas, números y
                símbolos tiene unos 79 bits de entropía: miles de millones de años de fuerza
                bruta con hardware actual si el sitio la guarda bien (con hash lento).
            """.trimIndent()
        ),

        // ── AES ─────────────────────────────────────────────
        "AES" to InfoEducativa(
            nombre       = "AES — Advanced Encryption Standard",
            categoria    = CategoriaAlgoritmo.SIMETRICO,
            añoCreacion  = 2001,
            creadores    = "Joan Daemen y Vincent Rijmen (Bélgica)",
            nivelSeguridad = NivelSeguridad.MUY_FUERTE,
            historia = """
                En 1997, el NIST (Instituto Nacional de Estándares de EE.UU.) lanzó 
                una competencia internacional para encontrar un sucesor al antiguo DES.
                
                Tras 3 años de evaluación con 15 candidatos de todo el mundo, en 2001 
                el algoritmo Rijndael de los criptógrafos belgas Daemen y Rijmen fue 
                seleccionado y adoptado como el nuevo estándar AES.
                
                Hoy, AES está en absolutamente todo: WhatsApp, tu banco, el WiFi, 
                los discos duros, y hasta los procesadores modernos tienen instrucciones 
                especiales de hardware para ejecutarlo más rápido.
            """.trimIndent(),
            comoFunciona = """
                AES opera sobre bloques de 128 bits (16 bytes) y puede usar claves 
                de 128, 192 o 256 bits. En CriptES usamos AES-256 (la versión más segura).
                
                El proceso tiene 14 "rondas" de transformación:
                
                1. SubBytes → Cada byte se sustituye por otro usando una tabla (S-Box)
                2. ShiftRows → Las filas de la matriz de datos se desplazan cíclicamente
                3. MixColumns → Las columnas se mezclan matemáticamente
                4. AddRoundKey → Se combina con la subclave de esa ronda (XOR)
                
                Estas 4 operaciones se repiten 14 veces, haciendo que cada bit del 
                resultado dependa de todos los bits de entrada.
            """.trimIndent(),
            fortalezas = listOf(
                "256 bits: necesitarías más tiempo del universo para romperlo por fuerza bruta",
                "Estándar mundial adoptado por gobiernos y la industria",
                "Muy eficiente: procesadores modernos lo aceleran por hardware",
                "Resistente a todos los ataques criptográficos conocidos"
            ),
            debilidades = listOf(
                "Requiere intercambiar la clave de forma segura (problema de la clave compartida)",
                "Si la contraseña es débil, el cifrado también lo es",
                "Vulnerable si se reutiliza el mismo IV (vector de inicialización)"
            ),
            casosDeUso = listOf(
                "Mensajería segura (WhatsApp, Signal)",
                "VPNs y comunicaciones cifradas",
                "Almacenamiento en discos duros (BitLocker, FileVault)",
                "Protección de archivos y contraseñas",
                "Transacciones bancarias online"
            ),
            datoCurioso = """
                Para romper AES-256 por fuerza bruta, necesitarías probar 
                2^256 ≈ 1.16 × 10^77 combinaciones. Si cada átomo del universo 
                observable fuera una computadora intentando una clave por segundo, 
                tardaría más que la edad del universo. 🌌
            """.trimIndent()
        ),

        // ── DES ─────────────────────────────────────────────
        "DES" to InfoEducativa(
            nombre       = "DES — Data Encryption Standard",
            categoria    = CategoriaAlgoritmo.SIMETRICO,
            añoCreacion  = 1977,
            creadores    = "IBM, adoptado por el NIST de EE.UU.",
            nivelSeguridad = NivelSeguridad.ROTO,
            historia = """
                DES fue el primer algoritmo de cifrado estandarizado públicamente 
                en 1977, basado en el diseño Lucifer de IBM. Fue adoptado por el 
                gobierno de EE.UU. como estándar oficial.
                
                Durante los años 80 y 90 fue el rey del cifrado simétrico. Sin embargo, 
                su clave de solo 56 bits era demasiado corta. En 1998, la EFF construyó 
                una máquina llamada "Deep Crack" que rompió DES en menos de 3 días 
                usando fuerza bruta. Costó solo $250,000 en hardware.
                
                Desde entonces DES está oficialmente retirado para uso de seguridad, 
                aunque su estudio es fundamental para entender la criptografía moderna.
            """.trimIndent(),
            comoFunciona = """
                DES usa una red de Feistel de 16 rondas sobre bloques de 64 bits.
                
                En cada ronda:
                1. El bloque se divide en mitad izquierda (L) y derecha (R)
                2. R pasa por una función compleja F con la subclave
                3. El resultado se combina con L usando XOR
                4. Las mitades se intercambian para la siguiente ronda
                
                La clave tiene 64 bits pero solo 56 son efectivos (8 son de paridad),
                lo que reduce enormemente el espacio de claves posibles.
            """.trimIndent(),
            fortalezas = listOf(
                "Diseño elegante que influyó en toda la criptografía posterior",
                "Muy rápido en hardware especializado",
                "Base histórica fundamental para entender 3DES y AES"
            ),
            debilidades = listOf(
                "Clave de solo 56 bits: rompible en horas con hardware moderno",
                "Completamente obsoleto para cualquier uso de seguridad real",
                "Vulnerable a ataques de criptoanálisis diferencial y lineal"
            ),
            casosDeUso = listOf(
                "Solo para fines educativos y estudio histórico",
                "Sistemas legados muy antiguos que aún no han migrado",
                "Comprensión de conceptos criptográficos básicos"
            ),
            datoCurioso = """
                Cuando el gobierno de EE.UU. adoptó DES en 1977, muchos criptógrafos 
                sospecharon que la NSA había reducido intencionalmente el tamaño de 
                la clave de 64 a 56 bits para poder romperlo ellos mismos en secreto. 
                Décadas después, documentos desclasificados confirmaron que la NSA 
                sí intervino en el diseño... aunque negaron haber añadido backdoors. 🕵️
            """.trimIndent()
        ),

        // ── RSA ─────────────────────────────────────────────
        "RSA" to InfoEducativa(
            nombre       = "RSA — Rivest–Shamir–Adleman",
            categoria    = CategoriaAlgoritmo.ASIMETRICO,
            añoCreacion  = 1977,
            creadores    = "Ron Rivest, Adi Shamir y Leonard Adleman (MIT)",
            nivelSeguridad = NivelSeguridad.FUERTE,
            historia = """
                En 1976, Whitfield Diffie y Martin Hellman publicaron el concepto 
                revolucionario de la criptografía de clave pública, pero sin una 
                implementación práctica.
                
                En 1977, tres matemáticos del MIT — Rivest, Shamir y Adleman — 
                pasaron una noche de Pésaj bebiendo vino y pensando en el problema. 
                A la mañana siguiente, Rivest tenía la idea de RSA.
                
                Curiosamente, el matemático británico Clifford Cocks ya había 
                inventado el mismo sistema en 1973 trabajando para el GCHQ (inteligencia 
                británica), pero fue clasificado como secreto de estado hasta 1997.
            """.trimIndent(),
            comoFunciona = """
                RSA se basa en la dificultad de factorizar números grandes:
                
                1. GENERACIÓN DE CLAVES:
                   • Se eligen dos números primos enormes p y q
                   • n = p × q (este número es público)
                   • Calcular n es trivial, pero factorizar n para recuperar p y q 
                     con n de 2048 bits es computacionalmente imposible hoy en día
                
                2. CIFRADO (con clave pública):
                   mensaje_cifrado = mensaje^e mod n
                   
                3. DESCIFRADO (con clave privada):
                   mensaje_original = mensaje_cifrado^d mod n
                   
                La magia está en que e y d son inversos matemáticos (mod φ(n)), 
                y calcular d sin conocer p y q requiere factorizar n.
            """.trimIndent(),
            fortalezas = listOf(
                "No requiere intercambio previo de secretos — clave pública puede publicarse",
                "Base de HTTPS, SSH, PGP y la seguridad de internet",
                "Permite firmas digitales y verificación de identidad",
                "2048 bits considerado seguro hasta ~2030 según NIST"
            ),
            debilidades = listOf(
                "Muy lento comparado con AES (1000x más lento para datos grandes)",
                "Limitado en tamaño: no puede cifrar mensajes largos directamente",
                "Vulnerable a computadoras cuánticas (algoritmo de Shor)",
                "Requiere generación cuidadosa de números primos"
            ),
            casosDeUso = listOf(
                "HTTPS/TLS para intercambiar claves simétricas de forma segura",
                "SSH para autenticación en servidores",
                "Email cifrado (PGP/GPG)",
                "Firma digital de documentos y software",
                "Certificados digitales y PKI"
            ),
            datoCurioso = """
                RSA publicó en 1991 una serie de retos: números RSA enormes que 
                prometían premios a quien los factorizara. El mayor roto hasta ahora 
                fue RSA-250 (829 bits) en 2020, usando cientos de computadoras 
                durante 2700 años de CPU-tiempo. RSA-2048 sigue intacto. 🔐
            """.trimIndent()
        ),

        // ── SHA-256 ──────────────────────────────────────────
        "SHA-256" to InfoEducativa(
            nombre       = "SHA-256 — Secure Hash Algorithm 256",
            categoria    = CategoriaAlgoritmo.HASH,
            añoCreacion  = 2001,
            creadores    = "NSA (Agencia de Seguridad Nacional de EE.UU.)",
            nivelSeguridad = NivelSeguridad.MUY_FUERTE,
            historia = """
                SHA-256 es parte de la familia SHA-2, publicada por la NSA en 2001 
                como mejora de SHA-1 (que a su vez mejoró el ya obsoleto MD5).
                
                El mundo lo conoció masivamente en 2009 cuando Satoshi Nakamoto 
                eligió SHA-256 como el corazón del protocolo Bitcoin. Cada bloque 
                de la blockchain se identifica por su hash SHA-256.
                
                En 2012, el NIST seleccionó Keccak como SHA-3, pero SHA-256 sigue 
                siendo el estándar más usado y no hay prisa por reemplazarlo.
            """.trimIndent(),
            comoFunciona = """
                SHA-256 procesa el mensaje en bloques de 512 bits:
                
                1. PADDING: Se rellena el mensaje hasta que su longitud sea 
                   congruente con 512 bits, añadiendo un '1' seguido de ceros 
                   y la longitud original al final.
                
                2. INICIALIZACIÓN: Se definen 8 valores hash iniciales H0-H7, 
                   derivados de las raíces cuadradas de los primeros 8 primos.
                
                3. COMPRESIÓN (por cada bloque):
                   64 rondas de operaciones bitwise (AND, OR, XOR, rotaciones)
                   mezclan el bloque con los valores hash actuales.
                
                4. RESULTADO: Después de procesar todos los bloques, 
                   los 8 valores hash concatenados forman los 256 bits del hash.
            """.trimIndent(),
            fortalezas = listOf(
                "Determinista: misma entrada SIEMPRE produce el mismo hash",
                "Unidireccional: matemáticamente imposible revertir",
                "Efecto avalancha: un cambio de 1 bit cambia el 50% del hash",
                "Sin colisiones conocidas: dos inputs distintos dan hashes distintos",
                "Estándar mundial en blockchain, SSL y verificación de software"
            ),
            debilidades = listOf(
                "No es una función de hash de contraseñas (demasiado rápido)",
                "Para contraseñas usar bcrypt, scrypt o Argon2",
                "Vulnerable a ataques de longitud de extensión si se usa mal"
            ),
            casosDeUso = listOf(
                "Bitcoin y la mayoría de criptomonedas",
                "Verificación de integridad de archivos descargados",
                "Certificados digitales y firmas",
                "Git (cada commit tiene un hash SHA)",
                "Almacenamiento seguro de contraseñas (combinado con salt)"
            ),
            datoCurioso = """
                El hash SHA-256 de una cadena vacía ("") siempre es:
                e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
                
                Si cambias UNA sola letra de cualquier texto, el resultado es 
                completamente diferente. Esto se llama "efecto avalancha". 🌊
            """.trimIndent()
        ),

        // ── Esteganografía ──────────────────────────────────
        "LSB" to InfoEducativa(
            nombre       = "Esteganografía LSB — Least Significant Bit",
            categoria    = CategoriaAlgoritmo.ESTEGANOGRAFIA,
            añoCreacion  = 1996,
            creadores    = "Técnica digital desarrollada con la era de las imágenes digitales",
            nivelSeguridad = NivelSeguridad.MODERADO,
            historia = """
                La esteganografía (del griego "escritura oculta") tiene 2500 años de historia.
                Los griegos afeitaban la cabeza de esclavos, tatuaban mensajes y esperaban 
                a que les creciera el pelo antes de enviarlos como mensajeros.
                
                En la era digital, LSB apareció en la década de 1990. El escándalo 
                más famoso fue en 2001 cuando el FBI sospechó que Al-Qaeda usaba 
                esteganografía en imágenes publicadas en eBay para coordinar ataques.
                
                Hoy se usa en marcas de agua digitales (watermarking) para proteger 
                derechos de autor de fotografías y videos.
            """.trimIndent(),
            comoFunciona = """
                Cada píxel de una imagen tiene 3 canales de color: Rojo (R), Verde (G), Azul (B).
                Cada canal vale entre 0 y 255 (8 bits).
                
                El bit menos significativo (LSB) de cada canal afecta solo en 1/255 
                al color — cambio imperceptible al ojo humano.
                
                PROCESO:
                1. Convertir el mensaje secreto a bits (unos y ceros)
                2. Por cada bit del mensaje, modificar el LSB de un canal de color
                3. El mensaje queda "escondido" en los datos de la imagen
                
                Ejemplo:
                Píxel original: R=200 (11001000)  ← queremos guardar el bit '1'
                Píxel modificado: R=201 (11001001) ← LSB cambiado a '1'
                
                Diferencia visual: 201 vs 200 en rojo... completamente invisible.
                
                Con 3 bits por píxel y una foto de 1MP, podemos ocultar ~375KB de texto.
            """.trimIndent(),
            fortalezas = listOf(
                "El mensaje es invisible al ojo humano",
                "La imagen parece completamente normal a cualquier observador",
                "Fácil de implementar y usar",
                "Se puede combinar con cifrado para doble seguridad"
            ),
            debilidades = listOf(
                "Herramientas de estegoanálisis pueden detectar patrones estadísticos",
                "Solo funciona bien con formato PNG (JPEG recomprime y destruye los LSBs)",
                "La imagen portadora no puede modificarse después",
                "Capacidad limitada por el tamaño de la imagen"
            ),
            casosDeUso = listOf(
                "Comunicación secreta entre partes",
                "Marcas de agua digitales (copyright)",
                "Canales encubiertos en redes (covert channels)",
                "Protección de metadatos sensibles en imágenes"
            ),
            datoCurioso = """
                Durante la Segunda Guerra Mundial, los nazis usaron el "punto microfoto" 
                (micropoint): reducían fotografías de documentos secretos al tamaño de 
                un punto tipográfico y lo pegaban en cartas inocentes.
                
                El FBI descubrió esta técnica en 1941 cuando un informante los alertó. 
                ¡La esteganografía lleva siglos burlando censores! 🕵️‍♀️
            """.trimIndent()
        )
    )

    /**
     * Obtiene la información educativa de un algoritmo por su nombre.
     * @param nombre Nombre del algoritmo (ej: "AES", "RSA", "SHA-256")
     * @return InfoEducativa o null si no existe
     */
    fun obtener(nombre: String): InfoEducativa? = algoritmos[nombre]
}
