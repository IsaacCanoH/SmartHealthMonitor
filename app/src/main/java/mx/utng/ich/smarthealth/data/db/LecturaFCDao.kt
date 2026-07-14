package mx.utng.ich.smarthealth.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LecturaFCDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lectura: LecturaFC): Long

    // Flow: actualización reactiva cuando hay nuevos datos
    @Query(
        """
        SELECT * FROM lecturas_fc
        ORDER BY timestamp DESC
        LIMIT 50
        """
    )
    fun obtenerUltimas(): Flow<List<LecturaFC>>

    @Query("SELECT * FROM lecturas_fc WHERE sincronizado = 0 ORDER BY timestamp ASC")
    suspend fun obtenerNoSincronizados(): List<LecturaFC>

    @Query(
        """
        UPDATE lecturas_fc
        SET sincronizado = 1, neonId = :neonId
        WHERE id = :id
        """
    )
    suspend fun marcarSincronizado(id: Int, neonId: Int)

    @Query(
        """
        UPDATE lecturas_fc
        SET valorBpm = :bpm,
            timestamp = :timestamp,
            hora = :hora,
            esNormal = :esNormal,
            estado = :estado,
            dispositivo = :dispositivo,
            sincronizado = 1
        WHERE neonId = :neonId
        """
    )
    suspend fun actualizarDesdeNeon(
        neonId: Int,
        bpm: Int,
        timestamp: Long,
        hora: String,
        esNormal: Boolean,
        estado: String,
        dispositivo: String
    ): Int

    @Query("SELECT COUNT(*) FROM lecturas_fc")
    suspend fun contarRegistros(): Int

    // Limpiar lecturas más antiguas de 7 días
    @Query(
        """
        DELETE FROM lecturas_fc
        WHERE timestamp < :limite
        """
    )
    suspend fun limpiarViejos(limite: Long)
}
