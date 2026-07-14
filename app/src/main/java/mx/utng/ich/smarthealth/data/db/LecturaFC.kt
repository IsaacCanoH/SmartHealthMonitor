package mx.utng.ich.smarthealth.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "lecturas_fc",
    indices = [Index(value = ["neonId"], unique = true)]
)
data class LecturaFC(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val valorBpm: Int,
    val timestamp: Long,
    val hora: String,
    val esNormal: Boolean,
    @ColumnInfo(defaultValue = "'Normal'")
    val estado: String = when {
        valorBpm < 60 -> "FC Baja"
        valorBpm > 100 -> "FC Alta"
        else -> "Normal"
    },
    @ColumnInfo(defaultValue = "'app'")
    val dispositivo: String = "app",
    @ColumnInfo(name = "sincronizado", defaultValue = "0")
    val sincronizado: Boolean = false,
    val neonId: Int? = null
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
                hora = horaActual,
                esNormal = valorBpm in 60..100,
                estado = when {
                    valorBpm < 60 -> "FC Baja"
                    valorBpm > 100 -> "FC Alta"
                    else -> "Normal"
                }
            )
        }
    }
}
