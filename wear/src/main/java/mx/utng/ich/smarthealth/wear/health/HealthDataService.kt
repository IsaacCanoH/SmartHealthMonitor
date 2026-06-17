package mx.utng.ich.smarthealth.wear.health

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.data.SampleDataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking
import mx.utng.ich.smarthealth.wear.data.WearDataSender
import mx.utng.ich.smarthealth.wear.data.WearHealthRepository

class HealthDataService : PassiveListenerService() {

    private lateinit var wearDataSender: WearDataSender

    override fun onCreate() {
        super.onCreate()
        wearDataSender = WearDataSender(this)
        Log.d("HealthDataService", "Servicio creado")
    }

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val fcDataPoints = dataPoints.getData(DataType.HEART_RATE_BPM)

        val ultimoDatoFC = fcDataPoints
            .filterIsInstance<SampleDataPoint<Double>>()
            .lastOrNull()

        if (ultimoDatoFC != null) {
            val bpm = ultimoDatoFC.value.toInt()

            // Actualiza la FC local del reloj para que la UI de Wear la pueda mostrar
            WearHealthRepository.actualizarFC(bpm)

            Log.d("HealthDataService", "FC recibida desde sensor: $bpm")
            Log.d("HealthDataService", "FC actualizada en WearHealthRepository: $bpm")

            // Envía la FC al teléfono por Wearable Data Layer
            runBlocking(Dispatchers.IO) {
                wearDataSender.enviarFC(bpm)
            }
        } else {
            Log.d("HealthDataService", "No llegó dato válido de FC")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("HealthDataService", "Servicio destruido")
    }

    companion object {
        suspend fun registrar(context: Context) {
            val hsClient = HealthServices.getClient(context)
            val passiveClient = hsClient.passiveMonitoringClient

            val config = PassiveListenerConfig.builder()
                .setDataTypes(setOf(DataType.HEART_RATE_BPM))
                .setShouldUserActivityInfoBeRequested(true)
                .build()

            passiveClient.setPassiveListenerServiceAsync(
                HealthDataService::class.java,
                config
            ).await()

            Log.d("HealthDataService", "HealthDataService registrado correctamente")
        }
    }
}