package mx.utng.smarthealthmonitor.tv.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class TvNeonRequest(
    val query: String,
    val params: List<Any?> = emptyList()
)

data class TvNeonResponse<T>(
    val rows: List<T> = emptyList(),
    val rowCount: Int = 0,
    val command: String = ""
)

data class TvLecturaFcDto(
    val id: Int = 0,
    val bpm: Int,
    val estado: String,
    val dispositivo: String,
    val hora: String,
    @SerializedName("created_at")
    val createdAt: String = ""
)

interface TvNeonApiService {

    @POST("sql")
    suspend fun executeQuery(
        @Header("Authorization") authorization: String,
        @Header("Neon-Connection-String") connectionString: String,
        @Body request: TvNeonRequest
    ): TvNeonResponse<TvLecturaFcDto>
}
