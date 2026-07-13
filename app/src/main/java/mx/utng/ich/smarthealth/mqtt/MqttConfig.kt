package mx.utng.ich.smarthealth.mqtt

import mx.utng.ich.smarthealth.BuildConfig

internal object MqttConfig {
    val brokerUrl: String = BuildConfig.MQTT_BROKER_URL
    val username: String = BuildConfig.MQTT_USERNAME
    val password: String = BuildConfig.MQTT_PASSWORD

    const val TOPIC_FC = "utng/smarthealthmonitor/fc"
    const val TOPIC_TV = "utng/smarthealthmonitor/tv"
    const val QOS = 1
    const val CLIENT_APP = "smarthealthmonitor-app"

    val isConfigured: Boolean
        get() = brokerUrl.startsWith("ssl://") && username.isNotBlank() && password.isNotBlank()
}
