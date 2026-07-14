package mx.utng.ich.smarthealth.wear.data.remote

import mx.utng.ich.smarthealth.wear.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object WearNeonClient {

    val authHeader: String
        get() = "Bearer ${BuildConfig.NEON_API_KEY}"

    val connectionString: String
        get() = BuildConfig.NEON_CONNECTION_STRING

    private val isConfigured: Boolean
        get() = BuildConfig.NEON_HOST.isNotBlank() &&
            BuildConfig.NEON_API_KEY.isNotBlank() &&
            connectionString.startsWith("postgresql://")

    private val httpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
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

    val api: WearNeonApiService by lazy {
        check(isConfigured) {
            "Neon no está configurado; revisa las propiedades NEON_* en local.properties"
        }

        Retrofit.Builder()
            .baseUrl("https://${BuildConfig.NEON_HOST}/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(WearNeonApiService::class.java)
    }
}
