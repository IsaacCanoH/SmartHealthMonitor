package mx.utng.ich.smarthealth.data.repository

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import mx.utng.ich.smarthealth.data.db.LecturaFC
import mx.utng.ich.smarthealth.data.db.LecturaFCDao
import mx.utng.ich.smarthealth.data.remote.LecturaFcDto
import mx.utng.ich.smarthealth.data.remote.NeonClient
import mx.utng.ich.smarthealth.data.remote.NeonRequest

class SyncRepository(
    private val dao: LecturaFCDao
) {

    /** Room es la fuente local reactiva y funciona sin conexión. */
    fun observarHistorial(): Flow<List<LecturaFC>> = dao.obtenerUltimas()

    /** Persiste primero en Room y deja pendiente cualquier envío fallido. */
    suspend fun insertarLectura(lectura: LecturaFC) {
        val localId = dao.insertar(
            lectura.copy(
                id = 0,
                sincronizado = false,
                neonId = null
            )
        ).toInt()

        try {
            val neonId = sincronizarHaciaNeon(lectura)
            dao.marcarSincronizado(localId, neonId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.w(TAG, "Lectura $localId pendiente de sincronización", exception)
        }
    }

    private suspend fun sincronizarHaciaNeon(lectura: LecturaFC): Int =
        withContext(Dispatchers.IO) {
            val response = NeonClient.api.executeQuery(
                connectionString = NeonClient.connectionString,
                request = NeonRequest(
                    query = """
                        INSERT INTO lecturas_fc (bpm, estado, dispositivo, hora)
                        VALUES ($1, $2, $3, $4)
                        RETURNING id
                    """.trimIndent(),
                    params = listOf(
                        lectura.valorBpm,
                        lectura.estado,
                        lectura.dispositivo,
                        lectura.hora
                    )
                )
            )

            response.rows.firstOrNull()?.id
                ?: error("Neon no devolvió el id de la lectura insertada")
        }

    /** Descarga las lecturas recientes y las combina por su id remoto. */
    suspend fun sincronizarDesdeNeon(limite: Int = 50) = withContext(Dispatchers.IO) {
        require(limite > 0) { "El límite debe ser mayor que cero" }

        val response = NeonClient.api.executeQuery(
            connectionString = NeonClient.connectionString,
            request = NeonRequest(
                query = """
                    SELECT id, bpm, estado, dispositivo, hora, created_at
                    FROM lecturas_fc
                    ORDER BY created_at DESC
                    LIMIT $1
                """.trimIndent(),
                params = listOf(limite)
            )
        )

        for (row in response.rows) {
            guardarDesdeNeon(row)
        }
        Log.d(TAG, "${response.rowCount} registros descargados de Neon")
    }

    /** Reintenta en orden las lecturas que todavía no llegaron a Neon. */
    suspend fun enviarPendientes() = withContext(Dispatchers.IO) {
        dao.obtenerNoSincronizados().forEach { lectura ->
            try {
                val neonId = sincronizarHaciaNeon(lectura)
                dao.marcarSincronizado(lectura.id, neonId)
                Log.d(TAG, "Sincronizada lectura pendiente id=${lectura.id}")
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.w(TAG, "Lectura ${lectura.id} continúa pendiente", exception)
            }
        }
    }

    private suspend fun guardarDesdeNeon(dto: LecturaFcDto) {
        val timestamp = dto.createdAt.takeIf(String::isNotBlank)?.let(::parseTimestamp)
            ?: System.currentTimeMillis()
        val esNormal = dto.bpm in 60..100

        val updated = dao.actualizarDesdeNeon(
            neonId = dto.id,
            bpm = dto.bpm,
            timestamp = timestamp,
            hora = dto.hora,
            esNormal = esNormal,
            estado = dto.estado,
            dispositivo = dto.dispositivo
        )
        if (updated == 0) {
            dao.insertar(
                LecturaFC(
                    valorBpm = dto.bpm,
                    timestamp = timestamp,
                    hora = dto.hora,
                    esNormal = esNormal,
                    estado = dto.estado,
                    dispositivo = dto.dispositivo,
                    sincronizado = true,
                    neonId = dto.id
                )
            )
        }
    }

    private fun parseTimestamp(value: String): Long? = runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }.parse(value)?.time
    }.getOrNull()

    private companion object {
        const val TAG = "SYNC"
    }
}
