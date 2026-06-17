package mx.utng.ich.smarthealth.wear.presentation

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import kotlinx.coroutines.launch
import mx.utng.ich.smarthealth.wear.health.HealthDataService
import mx.utng.ich.smarthealth.wear.presentation.theme.SmartHealthWearTheme

class MainActivity : ComponentActivity() {

    private val permisosLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->

            val permisoSensores =
                permisos[Manifest.permission.BODY_SENSORS] == true

            val permisoActividad =
                permisos[Manifest.permission.ACTIVITY_RECOGNITION] == true

            if (permisoSensores && permisoActividad) {
                registrarHealthServices()
            } else {
                Log.w(
                    "MainActivityWear",
                    "Permisos no concedidos: BODY_SENSORS=$permisoSensores, ACTIVITY_RECOGNITION=$permisoActividad"
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permisosLauncher.launch(
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        )

        setContent {
            WearApp()
        }
    }

    private fun registrarHealthServices() {
        lifecycleScope.launch {
            try {
                HealthDataService.registrar(applicationContext)
                Log.d("MainActivityWear", "Health Services registrado correctamente")
            } catch (e: Exception) {
                Log.e("MainActivityWear", "Error al registrar Health Services", e)
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun WearApp() {
    SmartHealthWearTheme {
        WearDashboardScreen(
            onAlertClick = {
                Log.d("MainActivityWear", "Chip de alerta presionado en Wear")
            }
        )
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@androidx.compose.runtime.Composable
fun DefaultPreview() {
    WearApp()
}