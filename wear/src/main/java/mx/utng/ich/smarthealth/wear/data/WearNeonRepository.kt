package mx.utng.ich.smarthealth.wear.data

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.utng.ich.smarthealth.wear.data.remote.WearLecturaFcDto
import mx.utng.ich.smarthealth.wear.data.remote.WearNeonClient
import mx.utng.ich.smarthealth.wear.data.remote.WearNeonRequest

class WearNeonRepository {

    suspend fun publicarLectura(bpm: Int, estado: String) = withContext(Dispatchers.IO) {
        val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        WearNeonClient.api.executeQuery(
            connectionString = WearNeonClient.connectionString,
            request = WearNeonRequest(
                query = """
                    INSERT INTO lecturas_fc (bpm, estado, dispositivo, hora)
                    VALUES ($1, $2, $3, $4)
                """.trimIndent(),
                params = listOf(bpm, estado, "wear", hora)
            )
        )
        Log.d(TAG, "FC enviada a Neon: $bpm bpm")
    }

    suspend fun obtenerUltimasLecturas(): List<WearLecturaFcDto> =
        withContext(Dispatchers.IO) {
            WearNeonClient.api.executeQuery(
                connectionString = WearNeonClient.connectionString,
                request = WearNeonRequest(
                    query = """
                        SELECT id, bpm, estado, dispositivo, hora
                        FROM lecturas_fc
                        WHERE dispositivo = 'wear'
                        ORDER BY created_at DESC
                        LIMIT 5
                    """.trimIndent()
                )
            ).rows
        }

    private companion object {
        const val TAG = "WEAR_DB"
    }
}
