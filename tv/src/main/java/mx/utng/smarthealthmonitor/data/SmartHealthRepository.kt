package mx.utng.smarthealthmonitor.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.utng.smarthealthmonitor.data.db.LecturaFC
import mx.utng.smarthealthmonitor.data.db.LecturaFCDao
import mx.utng.smarthealthmonitor.data.db.SmartHealthDB

object SmartHealthRepository {

    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    private var dao: LecturaFCDao? = null

    fun init(context: Context) {
        if (dao == null) {
            dao = SmartHealthDB.getDatabase(context).lecturaDao()
        }
    }

    suspend fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
        checkNotNull(dao) { "SmartHealthRepository no fue inicializado" }
            .insertar(LecturaFC.crear(bpm))
    }

    fun obtenerHistorial(): Flow<List<LecturaFC>> {
        return checkNotNull(dao) { "SmartHealthRepository no fue inicializado" }
            .obtenerUltimas()
    }
}
