package com.ecalar.listaviva.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ecalar.listaviva.utils.QRCodeGenerator

@Composable
fun QRCodeImage(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    val bitmap = remember(content) {
        QRCodeGenerator.generateQrBitmap(
            content = content,
            width = size.value.toInt(),
            height = size.value.toInt()
        )
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Código QR de invitación",
        modifier = modifier.size(size)
    )
}
