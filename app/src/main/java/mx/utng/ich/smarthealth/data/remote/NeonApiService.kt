package mx.utng.ich.smarthealth.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/** Petición parametrizada para el endpoint HTTP SQL de Neon. */
data class NeonRequest(
    val query: String,
    val params: List<Any?> = emptyList()
)

/** Resultado completo devuelto por el endpoint HTTP SQL de Neon. */
data class NeonResponse<T>(
    val rows: List<T> = emptyList(),
    val rowCount: Int = 0,
    val command: String = ""
)

/** DTO que representa una fila de la tabla lecturas_fc. */
data class LecturaFcDto(
    val id: Int = 0,
    val bpm: Int,
    val estado: String,
    val dispositivo: String,
    val hora: String,
    val fecha: String = "",
    @SerializedName("created_at")
    val createdAt: String = ""
)

interface NeonApiService {

    @POST("sql")
    suspend fun executeQuery(
        @Header("Authorization") authorization: String,
        @Header("Neon-Connection-String") connectionString: String,
        @Body request: NeonRequest
    ): NeonResponse<LecturaFcDto>
}
