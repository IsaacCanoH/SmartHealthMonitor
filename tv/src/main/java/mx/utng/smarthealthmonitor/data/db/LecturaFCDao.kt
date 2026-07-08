package mx.utng.smarthealthmonitor.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LecturaFCDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lectura: LecturaFC)

    @Query(
        """
        SELECT * FROM lecturas_fc
        ORDER BY timestamp DESC
        LIMIT 50
        """
    )
    fun obtenerUltimas(): Flow<List<LecturaFC>>

    @Query("SELECT * FROM lecturas_fc WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): LecturaFC?
}
