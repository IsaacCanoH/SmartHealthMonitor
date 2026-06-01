package mx.utng.ich.smarthealth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import mx.utng.ich.smarthealth.data.SmartHealthRepository
import mx.utng.ich.smarthealth.data.models.MockData

class DashboardViewModel : ViewModel() {

    // FC: viene del wearable real vía Repository.
    // Si es 0, significa que todavía no llegó dato real,
    // entonces se usa el valor simulado de MockData.
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) MockData.fcActual else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MockData.fcActual
        )

    // Pasos: viene del wearable real vía Repository.
    // Si es 0, se usa el valor simulado.
    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .map { if (it == 0) MockData.pasosActual else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MockData.pasosActual
        )

    // Historial simulado.
    // Más adelante, en S7, esto puede venir desde Room.
    val historial = MockData.historialFC
}