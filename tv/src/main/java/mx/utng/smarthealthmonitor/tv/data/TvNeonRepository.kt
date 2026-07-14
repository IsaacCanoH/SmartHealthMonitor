package mx.utng.smarthealthmonitor.tv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.utng.smarthealthmonitor.tv.data.remote.TvLecturaFcDto
import mx.utng.smarthealthmonitor.tv.data.remote.TvNeonClient
import mx.utng.smarthealthmonitor.tv.data.remote.TvNeonRequest

data class TvNeonItem(
    val id: Int,
    val bpm: Int,
    val estado: String,
    val dispositivo: String,
    val hora: String,
    val esEstadistica: Boolean = false
)

class TvNeonRepository {

    suspend fun obtenerHistorialCompleto(limite: Int = 50): List<TvLecturaFcDto> =
        withContext(Dispatchers.IO) {
            require(limite > 0) { "El límite debe ser mayor que cero" }
            TvNeonClient.api.executeQuery(
                authorization = TvNeonClient.authHeader,
                connectionString = TvNeonClient.connectionString,
                request = TvNeonRequest(
                    query = """
                        SELECT id, bpm, estado, dispositivo, hora, created_at
                        FROM lecturas_fc
                        ORDER BY created_at DESC
                        LIMIT $1
                    """.trimIndent(),
                    params = listOf(limite)
                )
            ).rows
        }

    suspend fun obtenerEstadisticas(): List<TvLecturaFcDto> =
        withContext(Dispatchers.IO) {
            TvNeonClient.api.executeQuery(
                authorization = TvNeonClient.authHeader,
                connectionString = TvNeonClient.connectionString,
                request = TvNeonRequest(
                    query = """
                        SELECT 0 AS id,
                               ROUND(AVG(bpm)) AS bpm,
                               'Promedio' AS estado,
                               dispositivo,
                               MAX(hora) AS hora
                        FROM lecturas_fc
                        GROUP BY dispositivo
                    """.trimIndent()
                )
            ).rows
        }
}

fun TvLecturaFcDto.toTvNeonItem(esEstadistica: Boolean = false) = TvNeonItem(
    id = id,
    bpm = bpm,
    estado = estado,
    dispositivo = dispositivo,
    hora = hora,
    esEstadistica = esEstadistica
)
