package mx.utng.ich.smarthealth.mqtt

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.SSLSocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mx.utng.ich.smarthealth.data.SmartHealthRepository
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttAppService(
    private val repository: SmartHealthRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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
                MqttConfig.CLIENT_APP,
                MemoryPersistence()
            )
        } catch (exception: MqttException) {
            Log.e(TAG, "No se pudo crear el cliente MQTT", exception)
            return
        }
        client = mqttClient
        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                subscribeToHeartRate(mqttClient)
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                if (topic == MqttConfig.TOPIC_FC) handleFcMessage(message)
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

    private fun subscribeToHeartRate(mqttClient: MqttAsyncClient) {
        try {
            mqttClient.subscribe(MqttConfig.TOPIC_FC, MqttConfig.QOS)
            Log.d(TAG, "Conectado y suscrito a ${MqttConfig.TOPIC_FC}")
        } catch (exception: MqttException) {
            Log.e(TAG, "No se pudo suscribir al topic de FC", exception)
        }
    }

    private fun handleFcMessage(message: MqttMessage) {
        val fcMessage = try {
            json.decodeFromString<FcMessage>(message.payload.decodeToString())
        } catch (exception: SerializationException) {
            Log.e(TAG, "Mensaje de FC inválido", exception)
            return
        }

        scope.launch {
            repository.actualizarFC(fcMessage.bpm)
            publishToTv(fcMessage)
        }
    }

    private fun publishToTv(fcMessage: FcMessage) {
        val mqttClient = client
        if (mqttClient?.isConnected != true) {
            Log.w(TAG, "No se re-publicó la FC: cliente MQTT desconectado")
            return
        }

        val tvMessage = TvMessage(
            bpm = fcMessage.bpm,
            estado = fcMessage.estado,
            hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        )
        val mqttMessage = MqttMessage(json.encodeToString(tvMessage).encodeToByteArray()).apply {
            qos = MqttConfig.QOS
            isRetained = true
        }

        try {
            mqttClient.publish(MqttConfig.TOPIC_TV, mqttMessage)
            Log.d(TAG, "Re-publicado al TV: ${fcMessage.bpm} bpm")
        } catch (exception: MqttException) {
            Log.e(TAG, "No se pudo re-publicar la FC al TV", exception)
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
            scope.cancel()
        }
    }

    private companion object {
        const val TAG = "MQTT_APP"
    }
}
