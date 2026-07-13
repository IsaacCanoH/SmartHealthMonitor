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

    fun connect() {
        if (!MqttConfig.isConfigured) {
            Log.w(TAG, "MQTT no configurado; agrega las credenciales a local.properties")
            return
        }
        if (client?.isConnected == true) return

        val mqttClient = try {
            MqttAsyncClient(
                MqttConfig.brokerUrl,
                MqttConfig.CLIENT_WEAR,
                MemoryPersistence()
            )
        } catch (exception: MqttException) {
            Log.e(TAG, "No se pudo crear el cliente MQTT", exception)
            return
        }
        client = mqttClient

        val options = MqttConnectOptions().apply {
            userName = MqttConfig.username
            password = MqttConfig.password.toCharArray()
            isCleanSession = true
            connectionTimeout = 30
            keepAliveInterval = 60
            socketFactory = SSLSocketFactory.getDefault()
        }

        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Conectado a HiveMQ Cloud")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Error al conectar con HiveMQ Cloud", exception)
                }
            })
        } catch (exception: MqttException) {
            client = null
            Log.e(TAG, "No se pudo iniciar la conexión MQTT", exception)
        }
    }

    fun publishFC(bpm: Int, estado: String) {
        val mqttClient = client
        if (mqttClient?.isConnected != true) {
            Log.w(TAG, "Lectura no publicada: cliente MQTT desconectado")
            return
        }

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
    }

    private companion object {
        const val TAG = "MQTT_WEAR"
    }
}
