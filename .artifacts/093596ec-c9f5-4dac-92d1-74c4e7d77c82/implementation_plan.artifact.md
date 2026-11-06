# Plan de Integración de Lógica de Red en Compose

El código proporcionado utiliza el paradigma antiguo de Android (XML y ViewBinding). Para mantener la alta calidad del diseño "Vintage Warm Rack" que hemos construido, integraré esa misma funcionalidad dentro de nuestra arquitectura de Jetpack Compose.

## Cambios Propuestos

### 1. Adaptación de `MainActivity`
- Inicializar `DeviceManager` usando `remember` en el Composable raíz (`GamerApp`) o como una propiedad de la clase `MainActivity`.
- Mantener la estructura de `ComponentActivity` y `setContent` para no romper la interfaz actual.

### 2. Integración en `WifiLinkScreen`
Traduciremos las acciones del código XML a nuestra pantalla profesional:
- **Descubrimiento (`btnDiscover`)**: Se activará automáticamente al entrar en la pantalla o mediante un botón de "RE-SCAN" con estética industrial.
- **Servidor (`btnStartServer`)**: Añadiremos un interruptor maestro (Master Switch) en el panel de WiFi para activar el modo servidor.
- **Conexión (`btnConnect`)**: Al pulsar "CONNECT" en un dispositivo de la lista real, se llamará a `deviceManager.connectToServer(ip)`.
- **Mensajería (`btnSend`)**: Prepararemos la lógica para enviar los datos de audio (dB) a través de `deviceManager.sendMessage()`.

### 3. Observación de Estado (Reactive UI)
- Usaremos `collectAsStateWithLifecycle()` para que la lista de dispositivos descubiertos y el estado de la conexión actualicen la interfaz de Compose automáticamente sin usar `lifecycleScope.launch` manualmente en la UI.

### 4. Gestión de Permisos
- Integraremos la solicitud de los nuevos permisos de red (`CHANGE_WIFI_STATE`, etc.) dentro del flujo de la app.

## User Review Required

> [!IMPORTANT]
> No reemplazaré el `MainActivity` actual por el código XML proporcionado, ya que eso borraría todo el diseño visual que hemos creado. En su lugar, **absorberé la lógica** de ese código para que funcione dentro de nuestras pantallas actuales.

## Plan de Verificación
- Confirmar que el radar de `WifiLinkScreen` reacciona al estado de escaneo real.
- Verificar que los dispositivos detectados por `DiscoveryService` aparecen en la lista de "AVAILABLE NODES".
- Probar la conexión entre dos dispositivos (uno como Server y otro como Client).
