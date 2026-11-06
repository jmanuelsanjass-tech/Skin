package com.example.skin

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.skin.ui.theme.SkinTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkinTheme {
                GamerApp()
            }
        }
    }
}

@Composable
fun GamerApp() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf("start") }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            currentScreen = "active"
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "start" -> {
                    FirstScreen(
                        onActivate = {
                            if (hasPermission) {
                                currentScreen = "active"
                            } else {
                                launcher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    )
                }
                "active" -> {
                    SecondScreen(onBack = { currentScreen = "start" })
                }
            }
        }
    }
}

@Composable
fun FirstScreen(onActivate: () -> Unit) {
    val charcoalWarm = Color(0xFF1A1A1A)
    val bronzeDark = Color(0xFF2D241E)
    val amberIntense = Color(0xFFFFBF00)
    val oldGold = Color(0xFFD4AF37)
    val creamVintage = Color(0xFFF5F5DC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(charcoalWarm)
            .drawBehind {
                // Sutil resplandor cálido central (Tube Glow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF4D3319).copy(alpha = 0.4f), Color.Transparent),
                        center = center,
                        radius = size.maxDimension * 0.6f
                    )
                )
                
                // Tornillos de bronce decorativos
                val screwRadius = 12f
                val margin = 40f
                val screwColors = listOf(oldGold, bronzeDark)
                
                val corners = listOf(
                    Offset(margin, margin),
                    Offset(size.width - margin, margin),
                    Offset(margin, size.height - margin),
                    Offset(size.width - margin, size.height - margin)
                )
                
                corners.forEach { pos ->
                    drawCircle(color = bronzeDark, radius = screwRadius + 2f, center = pos)
                    drawCircle(
                        brush = Brush.linearGradient(screwColors),
                        radius = screwRadius,
                        center = pos
                    )
                    // Ranura del tornillo
                    rotate(45f, pivot = pos) {
                        drawLine(
                            color = charcoalWarm.copy(alpha = 0.5f),
                            start = Offset(pos.x - 6f, pos.y),
                            end = Offset(pos.x + 6f, pos.y),
                            strokeWidth = 2f
                        )
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera Técnica
            Column(
                modifier = Modifier.padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = oldGold,
                    letterSpacing = 4.sp,
                    style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(Color.Black, Offset(2f, 2f), 4f))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "JMSJ TECHNOLOGY // MODEL AC-2026",
                    fontSize = 10.sp,
                    color = creamVintage.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )
            }

            // Interruptor Maestro Central
            MasterPowerButton(onClick = onActivate)

            // Pie de Página Técnico
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, end = 40.dp, bottom = 60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("INPUT: 24V DC", color = creamVintage.copy(alpha = 0.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("WARM-UP: REQUIRED", color = creamVintage.copy(alpha = 0.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "ANALOG SYSTEM v1.0",
                    color = amberIntense.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@Composable
fun MasterPowerButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "amber_glow")
    
    // Animación de respiración del LED
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "led_breathing"
    )

    // Animación de "latido" (Heartbeat) mientras espera
    val waitingPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waiting_pulse"
    )

    // Animación de brillo rotatorio (Scanning)
    val scanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_rotation"
    )

    // Animación de onda expansiva (Pulse Wave)
    val pulseWave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_wave"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else waitingPulse,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "button_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(220.dp)
                .drawBehind {
                    // Onda expansiva cálida
                    val baseRadius = size.minDimension / 2.2f
                    if (!isPressed) {
                        drawCircle(
                            color = Color(0xFFFFBF00).copy(alpha = (1f - pulseWave) * 0.3f),
                            radius = baseRadius * (1f + pulseWave * 0.5f),
                            center = center
                        )
                    }

                    rotate(if (isPressed) 2f else 0f) {
                        // Sombra exterior cálida
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.4f),
                            radius = (100.dp.toPx() * scale) + 10f,
                            center = center + Offset(4f, 4f)
                        )
                    }
                }
        ) {
            Canvas(modifier = Modifier.size(200.dp * scale)) {
                val radius = size.minDimension / 2
                
                // Anillo LED Ámbar (Glow)
                drawCircle(
                    color = Color(0xFFFFBF00).copy(alpha = glowAlpha * 0.2f),
                    radius = radius + 15f,
                    style = Stroke(width = 8f)
                )
                
                // Cuerpo del Botón (Aluminio Torneado)
                drawCircle(
                    brush = Brush.sweepGradient(
                        0.0f to Color(0xFF332B24),
                        0.2f to Color(0xFF4D3F35),
                        0.5f to Color(0xFF1A1613),
                        0.8f to Color(0xFF4D3F35),
                        1.0f to Color(0xFF332B24)
                    ),
                    radius = radius
                )
                
                // Borde de Bronce
                drawCircle(
                    color = Color(0xFFD4AF37).copy(alpha = 0.3f),
                    radius = radius,
                    style = Stroke(width = 2f)
                )

                // Resplandor rotatorio (Scanner)
                rotate(scanRotation) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to Color.Transparent,
                            0.1f to Color(0xFFFFBF00).copy(alpha = 0.4f),
                            0.2f to Color.Transparent
                        ),
                        startAngle = 0f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round),
                        size = Size(size.width + 10f, size.height + 10f),
                        topLeft = Offset(-5f, -5f)
                    )
                }

                // Círculos concéntricos de textura
                for (i in 1..5) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = radius * (i / 5.5f),
                        style = Stroke(width = 1f)
                    )
                }
            }

            // Etiqueta "POWER"
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POWER",
                    color = Color(0xFFD4AF37),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                // LED de Estado
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFBF00).copy(alpha = glowAlpha))
                        .shadow(4.dp, CircleShape, ambientColor = Color(0xFFFFBF00), spotColor = Color(0xFFFFBF00))
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "MASTER TOGGLE",
            color = Color(0xFFD4AF37).copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun UnifiedLedBar(value: Float) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "unified_led_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val numLeds = 80
            val spacing = 4f
            val ledWidth = (size.width - (numLeds - 1) * spacing) / numLeds
            
            for (i in 0 until numLeds) {
                val percent = i.toFloat() / (numLeds - 1)
                val isOn = animatedValue >= percent * 100f
                val x = i * (ledWidth + spacing)
                
                val ledColor = when {
                    percent < 0.6f -> if (isOn) Color(0xFF00FF00) else Color(0xFF003300)
                    percent < 0.85f -> if (isOn) Color(0xFFFFCC00) else Color(0xFF332B00)
                    else -> if (isOn) Color(0xFFFF0000) else Color(0xFF330000)
                }
                
                drawRect(
                    color = ledColor,
                    topLeft = Offset(x, 0f),
                    size = Size(ledWidth, size.height),
                    style = Fill
                )
                
                if (isOn) {
                    // Efecto de brillo para la tira unificada
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(ledColor.copy(0.4f), Color.Transparent)
                        ),
                        topLeft = Offset(x, -10f),
                        size = Size(ledWidth, 10f)
                    )
                }
            }
        }
    }
}

@Composable
fun SecondScreen(onBack: () -> Unit) {
    var dbLevel by remember { mutableFloatStateOf(0f) }
    var sensitivity by remember { mutableFloatStateOf(1.5f) }
    var isSliderVisible by remember { mutableStateOf(false) }
    var lastInteraction by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    @SuppressLint("MissingPermission")
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val bufferSize = AudioRecord.getMinBufferSize(
                54100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                54100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            val buffer = ShortArray(bufferSize)
            audioRecord.startRecording()

            try {
                while (isActive) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val amplitude = Math.sqrt(sum / read)
                        // Normalización dBFS aproximada: -60dB a 0dB escalado a 0-100
                        val db = if (amplitude > 0) 20 * log10(amplitude / 32768.0) + 60 else 0.0
                        val targetLevel = (db.toFloat() * sensitivity * 1.5f).coerceIn(0f, 100f)
                        
                        // Ballistics: Ataque instantáneo, Caída suave
                        if (targetLevel > dbLevel) {
                            dbLevel = targetLevel
                        } else {
                            dbLevel = dbLevel * 0.85f + targetLevel * 0.15f
                        }
                    }
                    delay(10) // Muestreo más frecuente para suavidad
                }
            } finally {
                audioRecord.stop()
                audioRecord.release()
            }
        }
    }

    LaunchedEffect(lastInteraction) {
        if (lastInteraction > 0) {
            isSliderVisible = true
            delay(1500)
            isSliderVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                lastInteraction = System.currentTimeMillis()
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tira LED Unificada Superior
            UnifiedLedBar(value = dbLevel)

            // Layout de Medidores
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    VintageNeedleMeter(value = dbLevel, label = "L_NODE")
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    VintageNeedleMeter(value = dbLevel * 0.98f, label = "R_NODE")
                }
            }

            // Espacio inferior para visualización
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Panel de Control Flotante (Sensibilidad) - Sin desplazar el Row superior
        AnimatedVisibility(
            visible = isSliderVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f) // No ocupa todo el ancho para verse más flotante
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SENSITIVITY",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "GAIN: ${(sensitivity).toString().take(4)}",
                        color = Color.White.copy(0.7f),
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = sensitivity,
                    onValueChange = { 
                        sensitivity = it
                        lastInteraction = System.currentTimeMillis()
                    },
                    valueRange = 0f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(0.1f)
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}

@Composable
fun VintageNeedleMeter(value: Float, label: String) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "vu_animation"
    )
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Cálculos dinámicos para centrar profesionalmente
            val componentWidth = size.width
            val componentHeight = size.height
            val radius = (minOf(componentWidth, componentHeight) * 0.85f) / 2
            val center = Offset(componentWidth / 2, componentHeight * 0.7f) // Centrado un poco más arriba para lucir mejor
            
            val arcWidth = radius * 2.64f
            val arcHeight = radius * 2.64f
            val arcTopLeft = Offset(center.x - arcWidth / 2, center.y - radius * 1.56f)

            // --- 1. Fondo de la Consola ---
            drawRect(color = Color(0xFF151515))

            // --- 2. Fondo del Dial (Amarillo Vintage) ---
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF7A1), Color(0xFFEEE885)),
                    center = center,
                    radius = radius * 1.8f
                ),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = arcTopLeft,
                size = Size(arcWidth, arcHeight),
                style = Fill
            )

            // --- 3. Borde Circular del Medidor ---
            drawArc(
                color = Color.Black,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = Size(arcWidth, arcHeight),
                style = Stroke(width = 4f)
            )

            // --- 5. Doble Escala (VU y %) ---
            val scaleStart = 205f
            val scaleSweep = 130f
            
            // Escala Superior VU
            val vuValues = listOf("20", "10", "7", "5", "3", "2", "1", "0", "3", "5")
            val vuAngles = listOf(0f, 0.15f, 0.25f, 0.35f, 0.45f, 0.55f, 0.65f, 0.75f, 0.88f, 1f)
            
            // Zona Roja
            drawArc(
                color = Color.Red.copy(0.8f),
                startAngle = scaleStart + (scaleSweep * 0.75f),
                sweepAngle = scaleSweep * 0.25f,
                useCenter = false,
                topLeft = Offset(center.x - (arcWidth * 0.85f) / 2, center.y - radius * 1.38f),
                size = Size(arcWidth * 0.85f, arcHeight * 0.85f),
                style = Stroke(width = 8f)
            )

            for (i in vuValues.indices) {
                val angle = scaleStart + (vuAngles[i] * scaleSweep)
                val rad = Math.toRadians(angle.toDouble())
                val isRed = i >= 7
                
                val outerR = radius * 1.32f
                val tickLen = radius * 0.18f
                val outerX = center.x + Math.cos(rad).toFloat() * outerR
                val outerY = center.y + Math.sin(rad).toFloat() * outerR
                val innerX = center.x + Math.cos(rad).toFloat() * (outerR - tickLen)
                val innerY = center.y + Math.sin(rad).toFloat() * (outerR - tickLen)

                drawLine(
                    color = if (isRed) Color.Red else Color.Black,
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = 3f
                )

                // Texto VU
                val textLayoutResult = textMeasurer.measure(
                    text = vuValues[i],
                    style = TextStyle(fontSize = (radius * 0.045f).sp, fontWeight = FontWeight.Bold, color = if (isRed) Color.Red else Color.Black)
                )
                val textR = outerR + radius * 0.22f
                val textX = center.x + Math.cos(rad).toFloat() * textR - textLayoutResult.size.width / 2
                val textY = center.y + Math.sin(rad).toFloat() * textR - textLayoutResult.size.height / 2
                drawText(textLayoutResult, topLeft = Offset(textX, textY))
            }

            // Escala Inferior %
            val percentValues = listOf("0", "20", "40", "60", "80", "100%")
            val percentR = radius * 1.02f
            for (i in percentValues.indices) {
                val angle = scaleStart + (i * (scaleSweep / 5f))
                val rad = Math.toRadians(angle.toDouble())
                
                val outerX = center.x + Math.cos(rad).toFloat() * percentR
                val outerY = center.y + Math.sin(rad).toFloat() * percentR
                
                val textLayoutResult = textMeasurer.measure(
                    text = percentValues[i],
                    style = TextStyle(fontSize = (radius * 0.045f).sp, fontWeight = FontWeight.Medium, color = Color.Black.copy(0.7f))
                )
                val textX = outerX - textLayoutResult.size.width / 2
                val textY = outerY - textLayoutResult.size.height / 2
                drawText(textLayoutResult, topLeft = Offset(textX, textY))
            }

            // --- 5. Etiqueta "VU" ---
            val vuTextLayout = textMeasurer.measure(
                text = "VU",
                style = TextStyle(fontSize = (radius * 0.2f).sp, fontWeight = FontWeight.Black, color = Color.Black.copy(0.6f))
            )
            drawText(vuTextLayout, topLeft = Offset(center.x - vuTextLayout.size.width / 2, center.y - radius * 0.72f))

            // --- 6. Detalles Mecánicos en la Base ---
            val baseWidth = radius * 1.2f
            drawRect(
                color = Color.Black,
                topLeft = Offset(center.x - baseWidth / 2, center.y - 10f),
                size = Size(baseWidth, 30f)
            )
            // Tornillos
            val screwOffset = baseWidth * 0.4f
            drawCircle(color = Color.LightGray, radius = 8f, center = Offset(center.x - screwOffset, center.y + 5f))
            drawCircle(color = Color.DarkGray, radius = 3f, center = Offset(center.x - screwOffset, center.y + 5f), style = Stroke(2f))
            drawCircle(color = Color.LightGray, radius = 8f, center = Offset(center.x + screwOffset, center.y + 5f))
            drawCircle(color = Color.DarkGray, radius = 3f, center = Offset(center.x + screwOffset, center.y + 5f), style = Stroke(2f))

            // --- 7. Aguja Anaranjada ---
            val rotationAngle = scaleStart + (animatedValue * (scaleSweep / 100f))
            
            // Sombra
            rotate(degrees = rotationAngle - 268f, pivot = center) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.2f),
                    start = center,
                    end = Offset(center.x, center.y - radius * 1.56f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            // Aguja
            rotate(degrees = rotationAngle - 270f, pivot = center) {
                drawLine(
                    color = Color(0xFFFF4500),
                    start = center,
                    end = Offset(center.x, center.y - radius * 1.62f),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
            }

            drawCircle(color = Color.Black, radius = 15f, center = center)
            drawCircle(color = Color.DarkGray, radius = 6f, center = center)
        }
        
        // Etiqueta del Canal
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }
}

