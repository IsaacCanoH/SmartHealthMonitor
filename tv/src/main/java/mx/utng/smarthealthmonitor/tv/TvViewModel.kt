package mx.utng.smarthealthmonitor.tv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.smarthealthmonitor.data.db.LecturaFC
import mx.utng.smarthealthmonitor.tv.data.TvNeonItem
import mx.utng.smarthealthmonitor.tv.data.TvNeonRepository
import mx.utng.smarthealthmonitor.tv.data.toTvNeonItem
import mx.utng.smarthealthmonitor.tv.mqtt.MqttTvSubscriber
import mx.utng.smarthealthmonitor.tv.mqtt.TvMessage

data class TvUiState(
    val fcActual: Int = 0,
    val fcEstado: String = "Sin datos",
    val ultimaHora: String = "--:--:--",
    val lecturasNeon: List<TvNeonItem> = emptyList(),
    val estadisticas: List<TvNeonItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class TvViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    private val mqttFlow = MutableStateFlow<TvMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(mqttFlow)
    private val neonRepository = TvNeonRepository()

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
        cargarDatos()
        viewModelScope.launch {
            mqttFlow.collect { tvMessage ->
                tvMessage ?: return@collect
                SmartHealthRepository.actualizarFC(tvMessage.bpm)
                _state.update {
                    it.copy(
                        fcActual = tvMessage.bpm,
                        fcEstado = tvMessage.estado,
                        ultimaHora = tvMessage.hora,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val lecturas = neonRepository.obtenerHistorialCompleto(50)
                val estadisticas = neonRepository.obtenerEstadisticas()
                _state.update {
                    it.copy(
                        lecturasNeon = lecturas.map { dto -> dto.toTvNeonItem() },
                        estadisticas = estadisticas.map { dto ->
                            dto.toTvNeonItem(esEstadistica = true)
                        },
                        isLoading = false,
                        error = null
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "No se pudo consultar Neon"
                    )
                }
            }
        }
    }

    fun refresh() = cargarDatos()

    override fun onCleared() {
        mqttSubscriber.disconnect()
        super.onCleared()
    }
}
