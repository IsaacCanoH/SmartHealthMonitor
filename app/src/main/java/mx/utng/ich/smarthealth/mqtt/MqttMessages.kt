package mx.utng.ich.smarthealth.mqtt

import kotlinx.serialization.Serializable

@Serializable
internal data class FcMessage(
    val bpm: Int,
    val estado: String,
    val timestamp: Long = 0L
)

@Serializable
internal data class TvMessage(
    val bpm: Int,
    val estado: String,
    val hora: String
)
