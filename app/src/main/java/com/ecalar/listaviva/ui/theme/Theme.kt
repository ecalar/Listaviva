package com.ecalar.listaviva.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Colores base
val NeoVerde = Color(0xFF4ADE80)
val NeoAzul = Color(0xFF3B82F6)
val NeoNaranja = Color(0xFFF97316)
val NeoMorado = Color(0xFFA855F7)
val AmarilloAlerta = Color(0xFFFACC15)
val RojoPeligro = Color(0xFFF87171)
val MoradoTarjeta = Color(0xFFC4B5FD)

val CremaFondo = Color(0xFFF4F0EA)
val BlancoPuro = Color(0xFFFFFFFF)
val NegroPuro = Color(0xFF000000)

val FondoOscuro = Color(0xFF18181B)
val SuperficieOscura = Color(0xFF27272A)
val TextoClaro = Color(0xFFF4F4F5)

private val DarkColorScheme = darkColorScheme(
    primary = NeoVerde,
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
    primary = NeoVerde,
    secondary = MoradoTarjeta,
    tertiary = AmarilloAlerta,
    background = CremaFondo,
    surface = CremaFondo,
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
    colorPrincipal: String = "Verde",
    tamanoTexto: String = "Normal", // <-- NUEVA VARIABLE
    content: @Composable () -> Unit
) {
    val primaryColor = when (colorPrincipal) {
        "Azul" -> NeoAzul
        "Naranja" -> NeoNaranja
        "Morado" -> NeoMorado
        else -> NeoVerde
    }

    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(primary = primaryColor)
    } else {
        LightColorScheme.copy(primary = primaryColor)
    }

    // Calculamos la escala basada en la preferencia
    val textScale = when (tamanoTexto) {
        "Pequeño" -> 0.85f
        "Grande" -> 1.2f
        else -> 1.0f
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getListaVivaTypography(textScale), // Aplicamos el factor de escala
        content = content
    )
}

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
            drawRoundRect(
                color = shadowColor,
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx),
                topLeft = androidx.compose.ui.geometry.Offset(offsetPx, offsetPx),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
        }
)