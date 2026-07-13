package mx.utng.smarthealthmonitor.tv.mqtt

import kotlinx.serialization.Serializable

@Serializable
data class TvMessage(
    val bpm: Int,
    val estado: String,
    val hora: String
)

@Serializable
internal data class FcMessage(
    val bpm: Int,
    val estado: String,
    val timestamp: Long = 0L
)
