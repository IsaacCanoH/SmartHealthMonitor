package mx.utng.ich.smarthealth.wear.mqtt

import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.net.ssl.SSLSocketFactory

class MqttWearPublisher {

    private var client: MqttAsyncClient? = null
    @Volatile private var pendingReading: Pair<Int, String>? = null
    @Volatile private var isConnecting = false

    fun connect() {
        if (!MqttConfig.isConfigured) {
            Log.w(TAG, "MQTT no configurado; agrega las credenciales a local.properties")
            return
        }
        if (client?.isConnected == true) return
        if (isConnecting) return
        isConnecting = true

        val mqttClient = try {
            MqttAsyncClient(
                MqttConfig.brokerUrl,
                MqttConfig.CLIENT_WEAR,
                MemoryPersistence()
            )
        } catch (exception: MqttException) {
            isConnecting = false
            Log.e(TAG, "No se pudo crear el cliente MQTT", exception)
            return
        }
        client = mqttClient

        val options = MqttConnectOptions().apply {
            userName = MqttConfig.username
            password = MqttConfig.password.toCharArray()
            isCleanSession = true
            isAutomaticReconnect = true
            connectionTimeout = 30
            keepAliveInterval = 60
            socketFactory = SSLSocketFactory.getDefault()
        }

        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    isConnecting = false
                    Log.d(TAG, "Conectado a HiveMQ Cloud")
                    pendingReading?.also { (bpm, estado) ->
                        pendingReading = null
                        publishConnected(mqttClient, bpm, estado)
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    isConnecting = false
                    client = null
                    Log.e(TAG, "Error al conectar con HiveMQ Cloud", exception)
                }
            })
        } catch (exception: MqttException) {
            isConnecting = false
            client = null
            Log.e(TAG, "No se pudo iniciar la conexión MQTT", exception)
        }
    }

    fun publishFC(bpm: Int, estado: String) {
        val mqttClient = client
        if (mqttClient?.isConnected != true) {
            pendingReading = bpm to estado
            Log.w(TAG, "Cliente MQTT desconectado; lectura pendiente: $bpm bpm")
            connect()
            return
        }

        publishConnected(mqttClient, bpm, estado)
    }

    private fun publishConnected(mqttClient: MqttAsyncClient, bpm: Int, estado: String) {
        val payload = Json.encodeToString(FcMessage(bpm = bpm, estado = estado)).encodeToByteArray()
        val mqttMessage = MqttMessage(payload).apply {
            qos = MqttConfig.QOS
            isRetained = true
        }

        try {
            mqttClient.publish(MqttConfig.TOPIC_FC, mqttMessage)
            Log.d(TAG, "Publicado: $bpm bpm -> ${MqttConfig.TOPIC_FC}")
        } catch (exception: MqttException) {
            Log.e(TAG, "No se pudo publicar la lectura de FC", exception)
        }
    }

    fun disconnect() {
        val mqttClient = client ?: return
        try {
            if (mqttClient.isConnected) {
                mqttClient.disconnect()
            }
        } catch (exception: MqttException) {
            Log.w(TAG, "No se pudo cerrar limpiamente la conexión MQTT", exception)
        }
        client = null
        isConnecting = false
    }

    private companion object {
        const val TAG = "MQTT_WEAR"
    }
}
