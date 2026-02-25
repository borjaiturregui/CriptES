# 🔐 CriptES — Kit de Seguridad Criptográfica para Android

<div align="center">

![CriptES Banner](https://img.shields.io/badge/CriptES-v1.0.0-darkred?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/Licencia-MIT-red?style=for-the-badge)
![API](https://img.shields.io/badge/API-26%2B-darkred?style=for-the-badge&logo=android)

**Aplicación Android de criptografía, hashing y esteganografía — completamente en español.**

*Construida con curiosidad, pasión por la seguridad y Jetpack Compose.*

</div>

---

## ✨ ¿Qué es CriptES?

**CriptES** es una navaja suiza de seguridad digital para Android. Nació como un proyecto personal de aprendizaje sobre criptografía y seguridad informática. No solo es una herramienta — cada función viene acompañada de un **modo educativo** que explica cómo funciona el algoritmo por dentro.

> 💡 *Aprende criptografía haciendo criptografía.*

---

## 📱 Funcionalidades

### 🔒 Cifrado Simétrico de Texto
Cifra y descifra texto con los algoritmos más usados en la industria:
- **AES-256** (Advanced Encryption Standard) — El estándar más seguro actual
- **DES** (Data Encryption Standard) — Clásico, ahora considerado débil
- **3DES** (Triple DES) — Mejora de DES con triple aplicación
- **ChaCha20** — Moderno y rápido, usado en TLS 1.3

### 🔑 Cifrado Asimétrico RSA
- Generación de par de claves (pública/privada)
- Cifrado con clave pública
- Descifrado con clave privada
- Exportación/importación de claves en formato PEM

### #️⃣ Generador de Hashes
Genera huellas digitales criptográficas de cualquier texto:
- **MD5** — Rápido, no recomendado para seguridad
- **SHA-1** — Obsoleto pero ampliamente estudiado
- **SHA-256** — Estándar actual (Bitcoin lo usa)
- **SHA-512** — Máxima seguridad en hashing

### 🖼️ Esteganografía en Imágenes
- Ocultar texto secreto dentro de imágenes PNG/JPG
- Extraer mensajes ocultos de imágenes
- Técnica LSB (Least Significant Bit)

### 📁 Cifrado de Archivos
- Cifrar cualquier archivo con AES-256
- Descifrar archivos protegidos
- Archivos guardados en `Descargas/criptes/`

### 📖 Modo Educativo
Cada módulo incluye una explicación detallada en español sobre:
- Historia del algoritmo
- Cómo funciona matemáticamente
- Casos de uso reales
- Fortalezas y debilidades

---

## 🏗️ Arquitectura

El proyecto sigue **Clean Architecture** con separación clara de responsabilidades:

```
com.criptes.app/
├── ui/                     # Capa de presentación (Jetpack Compose)
│   ├── tema/               # Colores, tipografía, tema oscuro
│   ├── navegacion/         # Navegación entre pantallas
│   ├── pantallas/          # Pantallas principales
│   └── componentes/        # Componentes reutilizables
├── dominio/                # Lógica de negocio
│   ├── modelos/            # Modelos de datos
│   └── casos_uso/          # Casos de uso (Use Cases)
├── datos/                  # Capa de datos
│   ├── repositorio/        # Implementación de repositorios
│   └── local/              # Room Database
├── criptografia/           # Motor criptográfico
│   ├── CifradoSimetrico.kt
│   ├── CifradoRSA.kt
│   ├── GeneradorHash.kt
│   └── Esteganografia.kt
└── di/                     # Inyección de dependencias (Hilt)
```

### Principios aplicados:
- **MVVM** (Model-View-ViewModel)
- **Clean Architecture** (UI → ViewModel → UseCase → Repository)
- **SOLID** principles
- **Inyección de dependencias** con Hilt
- **Coroutines + Flow** para operaciones asíncronas

---

## 🎨 Diseño

CriptES tiene una identidad visual propia e inconfundible:

| Elemento | Color |
|----------|-------|
| Fondo principal | `#000000` — Negro puro |
| Superficie cards | `#0F0F0F` — Negro profundo |
| Color primario | `#7B1A2E` — Rojo vino |
| Acento | `#B22948` — Rojo vino claro |
| Texto principal | `#FFFFFF` — Blanco |
| Texto secundario | `#9E9E9E` — Gris suave |

---

## 🚀 Instalación y Uso

### Prerrequisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17+
- Android SDK API 26+

### Clonar y ejecutar

```bash
# Clonar el repositorio
git clone https://github.com/borjaiturregui/CriptES.git

# Abrir en Android Studio
cd CriptES

# Sincronizar dependencias Gradle
./gradlew build

# Instalar en dispositivo/emulador
./gradlew installDebug
```

---

## 🛡️ Privacidad y Seguridad

- ✅ **Sin permisos de internet** — Todo funciona offline
- ✅ **Sin recopilación de datos** — Tu información nunca sale del dispositivo
- ✅ **Sin anuncios** — Proyecto 100% limpio
- ✅ **Código abierto** — Auditable por cualquiera
- ⚠️ Los archivos se guardan en `Descargas/criptes/`

---

## 📚 Dependencias Principales

| Librería | Versión | Uso |
|----------|---------|-----|
| Jetpack Compose BOM | 2024.02.00 | UI declarativa |
| Hilt | 2.51 | Inyección de dependencias |
| Room | 2.6.1 | Base de datos local |
| Navigation Compose | 2.7.7 | Navegación entre pantallas |
| Coroutines | 1.8.0 | Operaciones asíncronas |
| Bouncy Castle | 1.77 | Criptografía avanzada |

---

## 🛣️ Roadmap

- [x] Cifrado simétrico (AES, DES, 3DES, ChaCha20)
- [x] Generador de hashes (MD5, SHA-1, SHA-256, SHA-512)
- [x] Modo educativo en español
- [ ] Cifrado asimétrico RSA
- [ ] Cifrado de archivos
- [ ] Esteganografía LSB en imágenes
- [ ] Exportar/compartir claves RSA
- [ ] Generador de contraseñas seguras

---

## 🧠 ¿Por qué lo hice?

Quería entender la criptografía de verdad, no solo leer sobre ella. Este proyecto es mi laboratorio personal donde pongo en práctica conceptos de seguridad informática mientras aprendo Android con Jetpack Compose.

---

## 📄 Licencia

```
MIT License — Úsalo, modifícalo, compártelo.
```

---

<div align="center">

Hecho con 🖤 y mucha curiosidad criptográfica 🔐

</div>
