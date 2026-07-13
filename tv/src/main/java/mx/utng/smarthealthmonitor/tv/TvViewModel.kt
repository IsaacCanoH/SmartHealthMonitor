package mx.utng.smarthealthmonitor.tv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.smarthealthmonitor.data.db.LecturaFC
import mx.utng.smarthealthmonitor.tv.mqtt.MqttTvSubscriber
import mx.utng.smarthealthmonitor.tv.mqtt.TvMessage

data class TvUiState(
    val fcActual: Int = 0,
    val fcEstado: String = "Sin datos",
    val ultimaHora: String = "--:--:--",
    val isLoading: Boolean = true
)

class TvViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    private val mqttFlow = MutableStateFlow<TvMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(mqttFlow)

    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val historial: StateFlow<List<LecturaFC>> =
        SmartHealthRepository.obtenerHistorial()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    init {
        mqttSubscriber.connect()
        viewModelScope.launch {
            mqttFlow.collect { tvMessage ->
                tvMessage ?: return@collect
                SmartHealthRepository.actualizarFC(tvMessage.bpm)
                _state.update {
                    it.copy(
                        fcActual = tvMessage.bpm,
                        fcEstado = tvMessage.estado,
                        ultimaHora = tvMessage.hora,
                        isLoading = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        mqttSubscriber.disconnect()
        super.onCleared()
    }
}
