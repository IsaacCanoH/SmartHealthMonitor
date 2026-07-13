package mx.utng.smarthealthmonitor.tv.mqtt

import mx.utng.smarthealthmonitor.tv.BuildConfig

internal object MqttConfig {
    val brokerUrl: String = BuildConfig.MQTT_BROKER_URL
    val username: String = BuildConfig.MQTT_USERNAME
    val password: String = BuildConfig.MQTT_PASSWORD

    // Consumir la fuente original evita depender de que el telefono este
    // abierto para reenviar las lecturas.
    const val TOPIC_FC = "utng/smarthealthmonitor/fc"
    const val QOS = 1
    const val CLIENT_TV = "smarthealthmonitor-tv"

    val isConfigured: Boolean
        get() = brokerUrl.startsWith("ssl://") && username.isNotBlank() && password.isNotBlank()
}
