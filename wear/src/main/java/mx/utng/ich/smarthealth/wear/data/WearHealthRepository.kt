package mx.utng.ich.smarthealth.wear.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object WearHealthRepository {

    private val _fcFlow = MutableStateFlow(72)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    private val _historialFlow = MutableStateFlow<List<WearLecturaFC>>(emptyList())
    val historialFlow: StateFlow<List<WearLecturaFC>> = _historialFlow.asStateFlow()

    fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm

        val nuevaLectura = WearLecturaFC.crear(bpm)

        _historialFlow.value = listOf(nuevaLectura)
            .plus(_historialFlow.value)
            .take(20)
    }
}
