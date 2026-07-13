package mx.utng.ich.smarthealth.wear.mqtt

import mx.utng.ich.smarthealth.wear.BuildConfig

internal object MqttConfig {
    val brokerUrl: String = BuildConfig.MQTT_BROKER_URL
    val username: String = BuildConfig.MQTT_USERNAME
    val password: String = BuildConfig.MQTT_PASSWORD

    const val TOPIC_FC = "utng/smarthealthmonitor/fc"
    const val QOS = 1
    const val CLIENT_WEAR = "smarthealthmonitor-wear"

    val isConfigured: Boolean
        get() = brokerUrl.startsWith("ssl://") && username.isNotBlank() && password.isNotBlank()
}
