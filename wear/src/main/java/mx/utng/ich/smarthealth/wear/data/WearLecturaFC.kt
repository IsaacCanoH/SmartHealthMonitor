package mx.utng.ich.smarthealth.wear.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WearLecturaFC(
    val id: Long,
    val valorBpm: Int,
    val hora: String,
    val esNormal: Boolean
) {
    companion object {
        fun crear(valorBpm: Int): WearLecturaFC {
            val ahora = System.currentTimeMillis()

            val horaActual = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date(ahora))

            return WearLecturaFC(
                id = ahora,
                valorBpm = valorBpm,
                hora = horaActual,
                esNormal = valorBpm in 60..100
            )
        }
    }
}
