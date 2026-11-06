# Nota técnica del proyecto Skin

## 1. Resumen

Skin es una aplicación Android nativa con interfaz en Jetpack Compose. Presenta una consola visual de estilo retro/vintage y una pantalla de medición de nivel de audio que captura sonido desde el micrófono del dispositivo.

## 2. Tecnologías y configuración

- **Lenguaje:** Kotlin.
- **Interfaz:** Jetpack Compose, Material 3 y animaciones de Compose.
- **Plataforma:** Android; módulo de aplicación `:app`.
- **Identificador de aplicación y namespace:** `com.example.skin`.
- **SDK mínimo:** API 24.
- **SDK objetivo:** API 36.
- **SDK de compilación:** API 37.
- **Compatibilidad Java:** Java 11.
- **Build:** Android Gradle Plugin 9.3.1, Kotlin Compose plugin 2.2.10 y Gradle 9.5.
- **Dependencias principales:** AndroidX Core KTX, Activity Compose, Lifecycle Runtime KTX, Compose UI y Material 3.

Las versiones de dependencias se centralizan en `gradle/libs.versions.toml`. Gradle resuelve dependencias desde Google Maven y Maven Central.

## 3. Estructura relevante

- `app/src/main/java/com/example/skin/MainActivity.kt`: actividad, navegación entre pantallas, captura de audio y componentes visuales.
- `app/src/main/java/com/example/skin/ui/theme/`: paleta, tema Material y tipografía.
- `app/src/main/AndroidManifest.xml`: actividad de inicio y permisos de la aplicación.
- `app/src/test/`: pruebas locales JVM.
- `app/src/androidTest/`: pruebas instrumentadas Android.

## 4. Funcionamiento

1. `MainActivity` habilita el diseño edge-to-edge y presenta `GamerApp` con `SkinTheme`.
2. La pantalla inicial muestra una consola oscura con detalles ámbar y un botón principal animado.
3. Al activar el botón, la aplicación comprueba el permiso `RECORD_AUDIO` y lo solicita si hace falta. Si se concede, cambia a la pantalla de medidores.
4. En la pantalla de medición, `AudioRecord` lee audio mono PCM de 16 bits a 54.1 kHz. El procesamiento calcula el RMS de cada bloque, deriva un nivel aproximado y lo suaviza con ataque rápido y caída gradual.
5. El nivel alimenta una barra de 80 segmentos LED y dos medidores analógicos dibujados en Canvas. El control de sensibilidad aparece temporalmente después de una interacción.

La medición es una representación visual relativa del nivel digital recibido; no es una medición calibrada de presión sonora (dB SPL). Los medidores izquierdo y derecho muestran el mismo nivel, con una pequeña diferencia de escala en el segundo; no son canales estéreo independientes.

## 5. Permisos y datos

- El manifiesto declara el permiso `android.permission.RECORD_AUDIO`, solicitado durante la ejecución.
- En el código fuente revisado, la función principal de la app es la medición local del micrófono. No se observa una integración con backend ni persistencia propia de lecturas.

## 6. Pruebas y estado de validación

El proyecto contiene una prueba unitaria de ejemplo que verifica una suma y una prueba instrumentada de ejemplo que verifica el identificador de paquete. No cubren actualmente la captura de audio, la navegación ni los componentes de medición.

## 7. Observaciones técnicas

- La lógica de interfaz, captura y procesamiento de audio está concentrada en `MainActivity.kt`; separar la captura en un componente o capa de dominio facilitaría pruebas y mantenimiento.
- `SecondScreen` recibe una acción para volver a la pantalla inicial, pero el código visible no presenta un control que la invoque.
- Conviene validar los errores de inicialización y lectura de `AudioRecord` en dispositivos reales, además del ciclo de vida al abandonar la pantalla o detener la actividad.
- La apariencia de los medidores y el nivel mostrado deben considerarse indicadores visuales aproximados, no instrumentos de audio calibrados.
