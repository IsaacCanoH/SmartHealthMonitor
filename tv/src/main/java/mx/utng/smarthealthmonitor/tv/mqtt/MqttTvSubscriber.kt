package mx.utng.smarthealthmonitor.tv.mqtt

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.SSLSocketFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttTvSubscriber(
    private val tvFlow: MutableStateFlow<TvMessage?>
) {
    private val json = Json { ignoreUnknownKeys = true }
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
                MqttConfig.CLIENT_TV,
                MemoryPersistence()
            )
        } catch (exception: MqttException) {
            Log.e(TAG, "No se pudo crear el cliente MQTT", exception)
            return
        }
        client = mqttClient
        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                subscribeToTv(mqttClient)
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                if (topic != MqttConfig.TOPIC_FC) return

                val fcMessage = try {
                    json.decodeFromString<FcMessage>(message.payload.decodeToString())
                } catch (exception: SerializationException) {
                    Log.e(TAG, "Mensaje de TV inválido", exception)
                    return
                }
                if (fcMessage.bpm !in 20..250) return
                val instante = fcMessage.timestamp.takeIf { it > 0L } ?: System.currentTimeMillis()
                val tvMessage = TvMessage(
                    bpm = fcMessage.bpm,
                    estado = fcMessage.estado,
                    hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(instante))
                )
                tvFlow.value = tvMessage
                Log.d(TAG, "Recibido: ${tvMessage.bpm} bpm")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.w(TAG, "Conexión MQTT perdida", cause)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
        })

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

    private fun subscribeToTv(mqttClient: MqttAsyncClient) {
        try {
            mqttClient.subscribe(MqttConfig.TOPIC_FC, MqttConfig.QOS)
            Log.d(TAG, "TV suscrita a ${MqttConfig.TOPIC_FC}")
        } catch (exception: MqttException) {
            Log.e(TAG, "No se pudo suscribir al topic de TV", exception)
        }
    }

    fun disconnect() {
        val mqttClient = client
        try {
            if (mqttClient?.isConnected == true) mqttClient.disconnect()
        } catch (exception: MqttException) {
            Log.w(TAG, "No se pudo cerrar limpiamente la conexión MQTT", exception)
        } finally {
            client = null
        }
    }

    private companion object {
        const val TAG = "MQTT_TV"
    }
}
