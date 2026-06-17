package mx.utng.ich.smarthealth.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun SmartHealthWearTheme(
    content: @Composable () -> Unit
) {
    // Wear Material Theme — versión optimizada para pantallas circulares
    MaterialTheme(
        content = content
    )
}