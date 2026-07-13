package mx.utng.ich.smarthealth.wear.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.utng.ich.smarthealth.wear.data.WearDataSender
import mx.utng.ich.smarthealth.wear.data.WearHealthRepository
import mx.utng.ich.smarthealth.wear.data.WearLecturaFC
import mx.utng.ich.smarthealth.wear.mqtt.MqttWearPublisher

class WearDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dataSender = WearDataSender(application.applicationContext)
    private val mqttPublisher = MqttWearPublisher()

    private val _estadoEnvio = MutableStateFlow<String?>(null)
    val estadoEnvio: StateFlow<String?> = _estadoEnvio

    val fc: StateFlow<Int> = WearHealthRepository.fcFlow
        .map { if (it == 0) 72 else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 72
        )

    val historial: StateFlow<List<WearLecturaFC>> =
        WearHealthRepository.historialFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        mqttPublisher.connect()
        viewModelScope.launch {
            WearHealthRepository.historialFlow
                .mapNotNull { lecturas -> lecturas.firstOrNull()?.valorBpm }
                .collect { bpm ->
                    val estado = when {
                        bpm < 60 -> "FC Baja"
                        bpm > 100 -> "FC Alta"
                        else -> "Normal"
                    }
                    mqttPublisher.publishFC(bpm, estado)
                }
        }
    }

    fun enviarFrecuenciaCardiaca() {
        viewModelScope.launch {
            _estadoEnvio.value = "Enviando..."
            val enviado = dataSender.enviarFC(fc.value)
            _estadoEnvio.value = if (enviado) "FC enviada: ${fc.value} bpm" else "Teléfono no conectado"
        }
    }

    override fun onCleared() {
        mqttPublisher.disconnect()
        super.onCleared()
    }
}
