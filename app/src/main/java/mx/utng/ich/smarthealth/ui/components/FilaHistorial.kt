package mx.utng.ich.smarthealth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.utng.ich.smarthealth.data.models.LecturaFC
import mx.utng.ich.smarthealth.ui.theme.SmartHealthTheme

@Composable
fun FilaHistorial(
    lectura: LecturaFC,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Valor de frecuencia cardíaca con color según si es normal o no
        Text(
            text = "${lectura.valorBpm} bpm",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (lectura.esNormal)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.error
        )

        // Hora de la lectura
        Text(
            text = lectura.hora,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Preview(showBackground = true, name = "FilaHistorial - Normal")
@Composable
private fun FilaHistorialPreviewNormal() {
    SmartHealthTheme {
        FilaHistorial(
            lectura = LecturaFC(
                id = 1,
                valorBpm = 78,
                hora = "11:00"
            )
        )
    }
}

@Preview(showBackground = true, name = "FilaHistorial - Fuera de rango")
@Composable
private fun FilaHistorialPreviewFueraDeRango() {
    SmartHealthTheme {
        FilaHistorial(
            lectura = LecturaFC(
                id = 2,
                valorBpm = 120,
                hora = "10:30"
            )
        )
    }
}