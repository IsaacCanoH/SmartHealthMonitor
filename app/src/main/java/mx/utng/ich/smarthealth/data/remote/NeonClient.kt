package mx.utng.ich.smarthealth.data.remote

import mx.utng.ich.smarthealth.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NeonClient {

    val connectionString: String
        get() = BuildConfig.NEON_CONNECTION_STRING

    val isConfigured: Boolean
        get() = BuildConfig.NEON_HOST.isNotBlank() &&
            connectionString.startsWith("postgresql://")

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        redactHeader("Neon-Connection-String")
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val api: NeonApiService by lazy {
        check(isConfigured) {
            "Neon no está configurado; revisa las propiedades NEON_* en local.properties"
        }

        Retrofit.Builder()
            .baseUrl("https://${BuildConfig.NEON_HOST}/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(NeonApiService::class.java)
    }
}
