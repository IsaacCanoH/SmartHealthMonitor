package mx.utng.ich.smarthealth.wear.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object WearHealthRepository {

    private val _fcFlow = MutableStateFlow(72)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
    }
}