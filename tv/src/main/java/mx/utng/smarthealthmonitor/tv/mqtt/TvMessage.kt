package mx.utng.smarthealthmonitor.tv.mqtt

import kotlinx.serialization.Serializable

@Serializable
data class TvMessage(
    val bpm: Int,
    val estado: String,
    val hora: String
)
