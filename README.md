# Skin

Aplicación Android nativa con interfaz retro/vintage y medidores visuales del nivel de audio capturado por el micrófono.

## Nota técnica

### Tecnologías y configuración

- **Lenguaje:** Kotlin.
- **Interfaz:** Jetpack Compose, Material 3 y animaciones de Compose.
- **Módulo:** `:app`.
- **Identificador de aplicación y namespace:** `com.example.skin`.
- **SDK mínimo:** API 24.
- **SDK objetivo:** API 36.
- **SDK de compilación:** API 37.
- **Compatibilidad Java:** Java 11.
- **Build:** Android Gradle Plugin 9.3.1, Kotlin Compose plugin 2.2.10 y Gradle 9.5.
- **Dependencias principales:** AndroidX Core KTX, Activity Compose, Lifecycle Runtime KTX, Compose UI y Material 3.

Las versiones de dependencias se centralizan en `gradle/libs.versions.toml`; Gradle resuelve dependencias desde Google Maven y Maven Central.

### Estructura relevante

- `app/src/main/java/com/example/skin/MainActivity.kt`: actividad, navegación, captura de audio y componentes visuales.
- `app/src/main/java/com/example/skin/ui/theme/`: paleta, tema Material y tipografía.
- `app/src/main/AndroidManifest.xml`: actividad de inicio y permisos.
- `app/src/test/`: pruebas locales JVM.
- `app/src/androidTest/`: pruebas instrumentadas Android.

### Funcionamiento

1. `MainActivity` habilita el diseño edge-to-edge y presenta `GamerApp` con `SkinTheme`.
2. La pantalla inicial muestra una consola oscura con detalles ámbar y un botón principal animado.
3. Al activar el botón, la aplicación comprueba el permiso `RECORD_AUDIO` y lo solicita si hace falta. Si se concede, cambia a la pantalla de medidores.
4. `AudioRecord` lee audio mono PCM de 16 bits a 54.1 kHz. El procesamiento calcula el RMS de cada bloque, deriva un nivel aproximado y lo suaviza con ataque rápido y caída gradual.
5. El nivel alimenta una barra de 80 segmentos LED y dos medidores analógicos dibujados en Canvas. El control de sensibilidad aparece temporalmente después de una interacción.

La medición es una representación visual relativa del nivel digital recibido; no es una medición calibrada de presión sonora (dB SPL). Los medidores izquierdo y derecho muestran el mismo nivel, con una pequeña diferencia de escala en el segundo; no son canales estéreo independientes.

### Permisos y datos

- El manifiesto declara el permiso `android.permission.RECORD_AUDIO`, solicitado durante la ejecución.
- La función principal es la medición local del micrófono. En el código fuente revisado no se observa integración con backend ni persistencia propia de lecturas.

### Pruebas

El proyecto contiene una prueba unitaria de ejemplo que verifica una suma y una prueba instrumentada de ejemplo que verifica el identificador de paquete. No cubren actualmente la captura de audio, la navegación ni los componentes de medición.

### Observaciones técnicas

- La lógica de interfaz, captura y procesamiento de audio está concentrada en `MainActivity.kt`; separar la captura en un componente o capa de dominio facilitaría pruebas y mantenimiento.
- `SecondScreen` recibe una acción para volver a la pantalla inicial, pero el código visible no presenta un control que la invoque.
- Conviene validar los errores de inicialización y lectura de `AudioRecord` en dispositivos reales, además del ciclo de vida al abandonar la pantalla o detener la actividad.
- Los medidores y el nivel mostrado deben considerarse indicadores visuales aproximados, no instrumentos de audio calibrados.

La nota técnica también está disponible en [NOTA_TECNICA.md](NOTA_TECNICA.md).
