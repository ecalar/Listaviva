package com.ecalar.listaviva.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// --- PALETA DE COLORES NEO-BRUTALISTA ---
// Colores vibrantes comunes para ambos modos
val VerdeAccion = Color(0xFF4ADE80)
val AmarilloAlerta = Color(0xFFFACC15)
val RojoPeligro = Color(0xFFF87171)
val MoradoTarjeta = Color(0xFFC4B5FD)

// Colores Modo Claro
val CremaFondo = Color(0xFFF4F0EA)
val BlancoPuro = Color(0xFFFFFFFF)
val NegroPuro = Color(0xFF000000)

// Colores Modo Oscuro
val FondoOscuro = Color(0xFF18181B)       // Gris casi negro
val SuperficieOscura = Color(0xFF27272A)  // Gris oscuro para las tarjetas
val TextoClaro = Color(0xFFF4F4F5)        // Blanco roto para textos en modo oscuro

private val DarkColorScheme = darkColorScheme(
    primary = VerdeAccion,
    secondary = MoradoTarjeta,
    tertiary = AmarilloAlerta,
    background = FondoOscuro,
    surface = SuperficieOscura,
    onPrimary = NegroPuro,
    onSecondary = NegroPuro,
    onTertiary = NegroPuro,
    onBackground = TextoClaro,
    onSurface = TextoClaro,
    error = RojoPeligro,
    onError = NegroPuro
)

private val LightColorScheme = lightColorScheme(
    primary = VerdeAccion,
    secondary = MoradoTarjeta,
    tertiary = AmarilloAlerta,
    background = CremaFondo,
    surface = CremaFondo, // El Scaffold usa crema, las tarjetas usarán blanco
    onPrimary = NegroPuro,
    onSecondary = NegroPuro,
    onTertiary = NegroPuro,
    onBackground = NegroPuro,
    onSurface = NegroPuro,
    error = RojoPeligro,
    onError = BlancoPuro
)

@Composable
fun ListaVivaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos el color dinámico para asegurar que se respeta nuestra paleta brutalista
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de tener tu archivo Typography.kt configurado
        content = content
    )
}

// --- MODIFICADOR NEO-BRUTALISTA REUTILIZABLE ---
/**
 * Extensión de Modifier para aplicar bordes gruesos y sombra dura
 * desplazada típica del diseño Neo-Brutalista.
 */
fun Modifier.neoBrutalism(
    cornerRadius: Dp = 12.dp,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 6.dp,
    borderColor: Color = NegroPuro,
    shadowColor: Color = NegroPuro
): Modifier = this.then(
    Modifier
        .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(cornerRadius))
        .drawBehind {
            val offsetPx = shadowOffset.toPx()
            val radiusPx = cornerRadius.toPx()

            // Dibuja la sombra sólida detrás del componente
            drawRoundRect(
                color = shadowColor,
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx),
                topLeft = androidx.compose.ui.geometry.Offset(offsetPx, offsetPx),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
        }
)