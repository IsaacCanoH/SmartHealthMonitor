package mx.utng.ich.smarthealth.wear.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class WearNeonRequest(
    val query: String,
    val params: List<Any?> = emptyList()
)

data class WearNeonResponse<T>(
    val rows: List<T> = emptyList(),
    val rowCount: Int = 0,
    val command: String = ""
)

data class WearLecturaFcDto(
    val id: Int = 0,
    val bpm: Int,
    val estado: String,
    val dispositivo: String,
    val hora: String
)

interface WearNeonApiService {

    @POST("sql")
    suspend fun executeQuery(
        @Header("Authorization") authorization: String,
        @Header("Neon-Connection-String") connectionString: String,
        @Body request: WearNeonRequest
    ): WearNeonResponse<WearLecturaFcDto>
}
