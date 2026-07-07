package mx.utng.smarthealthmonitor.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "lecturas_fc")
data class LecturaFC(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val valorBpm: Int,
    val timestamp: Long,
    val hora: String,
    val esNormal: Boolean = valorBpm in 60..100
) {
    companion object {
        fun crear(valorBpm: Int): LecturaFC {
            val ahora = System.currentTimeMillis()
            val horaActual = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date(ahora))

            return LecturaFC(
                valorBpm = valorBpm,
                timestamp = ahora,
                hora = horaActual
            )
        }
    }
}
