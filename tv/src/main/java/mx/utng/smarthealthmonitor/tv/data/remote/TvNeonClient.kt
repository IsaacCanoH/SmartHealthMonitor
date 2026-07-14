package mx.utng.smarthealthmonitor.tv.data.remote

import mx.utng.smarthealthmonitor.tv.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object TvNeonClient {

    val connectionString: String
        get() = BuildConfig.NEON_CONNECTION_STRING

    private val isConfigured: Boolean
        get() = BuildConfig.NEON_HOST.isNotBlank() &&
            connectionString.startsWith("postgresql://")

    private val httpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            redactHeader("Neon-Connection-String")
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    val api: TvNeonApiService by lazy {
        check(isConfigured) {
            "Neon no está configurado; revisa las propiedades NEON_* en local.properties"
        }

        Retrofit.Builder()
            .baseUrl("https://${BuildConfig.NEON_HOST}/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(TvNeonApiService::class.java)
    }
}
