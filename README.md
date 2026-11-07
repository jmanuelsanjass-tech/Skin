# Skin - Ultimate Hi-Fi Audio System & Music Player

Aplicación Android nativa desarrollada en Kotlin y Jetpack Compose que combina un reproductor de música de alta fidelidad con integración completa de **Navidrome (Subsonic API)**, reproducción de archivos locales del dispositivo (FLAC, MP3, WAV, AAC, OGG), efectos de audio avanzados, medidores analógicos VU en tiempo real y notificaciones multimedia en segundo plano.

---

## Nota Técnica

### Tecnologías y Configuración

- **Lenguaje:** Kotlin.
- **Interfaz:** Jetpack Compose, Material 3, animaciones avanzadas y diseño minimalista azul cálido (`Primary = Color(0xFF3B82F6)`, `Background = Color(0xFF0F172A)`).
- **Reproducción de Audio:** AndroidX Media3 ExoPlayer y `MediaSession` (para control en segundo plano, pantalla de bloqueo y botones de hardware).
- **Streaming y API:** Retrofit 2, OkHttp 3 y API Subsonic (Navidrome).
- **Módulo:** `:app`.
- **Identificador y Namespace:** `com.example.skin`.
- **SDK Mínimo:** API 24.
- **SDK Objetivo / Compilación:** API 36 / API 37.
- **Compatibilidad Java:** Java 11.

---

### Módulos y Arquitectura

1. **Navidrome Client (`com.example.skin.navidrome`)**:
   - `NavidromeApiService.kt`: Clientes Retrofit para Subsonic (`ping`, `getArtists`, `getArtist`, `getAlbumList2`, `getAlbum`, `search3`, `getPlaylists`, `getGenres`, `getSongsByGenre`, `getStarred2`, `star`, `unstar`). Generación de tokens MD5 con salt.
   - `MusicPlayerManager.kt`: Gestor de ExoPlayer que maneja reproducción por streaming (Navidrome) o archivos locales (`/path/...`), colas de reproducción, bucle (`repeatMode`), reproducción aleatoria (`shuffle`) y efectos de audio.

2. **Efectos de Audio (`com.example.skin.audiofx`)**:
   - `AudioEffectsManager.kt`: Envoltorio para ecualizador gráfico de bandas (presets Rock, Pop, Jazz, Clásica), Realce de Graves (*Bass Boost*) y Sonido Envolvente (*Surround Sound*).

3. **Interfaz de Usuario y Pantallas (`MainActivity.kt`)**:
   - **Consola Principal / Hub**: Permite alternar entre el **Medidor Analógico de Audio (VU Meters)** y el **Reproductor Hi-Fi Navidrome**.
   - **Medidor Analógico (VU Meter)**: Captura de audio en tiempo real desde el micrófono mediante `AudioRecord` (PCM de 16 bits a 54.1 kHz), cálculo de RMS/dB, barra LED de 80 segmentos y agujas analógicas móviles en Canvas.
   - **Biblioteca Hi-Fi**: Pestañas deslizantes (`LazyRow`) para **Albums**, **Artists**, **Device** (audio local del teléfono), **Genres** y **Favs**, con barra de búsqueda rápida y opciones de ordenamiento.
   - **Reproductor Glass Deck**: Diseño inspirado en interfaces de alta fidelidad, con carátula del álbum, barra de progreso interactiva, letras sincronizadas (Karaoke LRC), panel de ecualización y cola de reproducción con opciones de descarga.
   - **Descarga de Audio**: Funcionalidad de descarga de pistas individuales o masivas (`Descarga Masiva`) con cuadro de diálogo emergente para seleccionar la carpeta de destino en el almacenamiento del dispositivo.

---

### Permisos y Configuración de Red

- `RECORD_AUDIO`: Captura de audio por micrófono para los vúmetros analógicos.
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_AUDIO`: Lectura y reproducción de archivos de audio locales del dispositivo.
- Soporte para tráfico HTTP de red local (configurado en `network_security_config.xml`).

---

### Pruebas y Verificación

El proyecto compila y se despliega correctamente con `app:assembleDebug`, integrando control multimedia a nivel de sistema operativo y procesamiento de audio de alta fidelidad.
