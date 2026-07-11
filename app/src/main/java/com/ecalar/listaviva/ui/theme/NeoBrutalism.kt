package com.ecalar.listaviva.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modificador que aplica la estética neo-brutalista:
 * Un borde negro grueso y una sombra sólida desplazada.
 */
fun Modifier.neoBrutalism(
    cornerRadius: Dp = 12.dp,
    borderWidth: Dp = 3.dp,
    shadowOffset: Dp = 6.dp,
    shadowColor: Color = Color.Black
): Modifier = this
    .drawBehind {
        // Dibuja la sombra sólida dura detrás del componente
        drawRoundRect(
            color = shadowColor,
            topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
            size = size,
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        )
    }
    .border(
        width = borderWidth,
        color = Color.Black,
        shape = RoundedCornerShape(cornerRadius)
    )