package mx.utng.ich.smarthealth.wear.mqtt

import kotlinx.serialization.Serializable

@Serializable
internal data class FcMessage(
    val bpm: Int,
    val estado: String,
    val timestamp: Long = System.currentTimeMillis()
)
