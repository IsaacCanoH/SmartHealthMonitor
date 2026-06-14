package mx.utng.ich.smarthealth.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.utng.ich.smarthealth.ui.theme.SmartHealthTheme

@Composable
fun AlertaScreen(
    fc: Int, // FC actual del Dashboard
    onDismiss: () -> Unit, // Cancelar / cerrar
    onConfirmar: () -> Unit // Confirmar y enviar alerta
) {
    var enviando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Enviar alerta de emergencia",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "FC actual: $fc bpm",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Se notificará a tus contactos de emergencia.\n" +
                            "Esta acción no se puede deshacer."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    enviando = true
                    onConfirmar()
                },
                enabled = !enviando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                if (enviando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "CONFIRMAR ALERTA",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !enviando
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(
    showBackground = true,
    name = "Alerta - Light",
    widthDp = 360,
    heightDp = 640
)
@Preview(
    showBackground = true,
    name = "Alerta - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 360,
    heightDp = 640
)
@Composable
private fun AlertaScreenPreview() {
    SmartHealthTheme {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AlertaScreen(
                fc = 145,
                onDismiss = { },
                onConfirmar = { }
            )
        }
    }
}