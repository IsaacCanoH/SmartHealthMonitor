package mx.utng.ich.smarthealth.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import mx.utng.ich.smarthealth.data.db.LecturaFC
import mx.utng.ich.smarthealth.data.db.LecturaFCDao
import mx.utng.ich.smarthealth.data.db.SmartHealthDB

/**
 * Repositorio singleton que centraliza los datos de salud.
 * El WearListenerService escribe aquí.
 * El ViewModel lee de aquí.
 * Room guarda el historial persistente.
 */
object SmartHealthRepository {

    // FC actual del wearable (bpm)
    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    // Pasos del día actual
    private val _pasosFlow = MutableStateFlow(0)
    val pasosFlow: StateFlow<Int> = _pasosFlow.asStateFlow()

    // DAO de Room
    private var dao: LecturaFCDao? = null

    /**
     * Inicializa Room.
     * Se debe llamar una sola vez desde SmartHealthApp.
     */
    fun init(context: Context) {
        dao = SmartHealthDB.getDatabase(context).lecturaDao()
    }

    /**
     * Actualiza la frecuencia cardíaca actual
     * y la guarda automáticamente en Room.
     */
    suspend fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm

        dao?.insertar(
            LecturaFC.crear(bpm)
        )
    }

    /**
     * Actualiza los pasos actuales.
     * Por ahora no se guardan en Room.
     */
    fun actualizarPasos(pasos: Int) {
        _pasosFlow.value = pasos
    }

    /**
     * Obtiene el historial de las últimas lecturas desde Room.
     */
    fun obtenerHistorial(): Flow<List<LecturaFC>> {
        return dao?.obtenerUltimas() ?: emptyFlow()
    }
}