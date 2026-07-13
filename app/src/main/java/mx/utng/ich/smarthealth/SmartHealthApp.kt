package mx.utng.ich.smarthealth

import android.app.Application
import mx.utng.ich.smarthealth.data.SmartHealthRepository
import mx.utng.ich.smarthealth.mqtt.MqttAppService

class SmartHealthApp : Application() {

    private lateinit var mqttService: MqttAppService

    override fun onCreate() {
        super.onCreate()

        SmartHealthRepository.init(this)
        mqttService = MqttAppService(SmartHealthRepository)
        mqttService.connect()
    }

    override fun onTerminate() {
        mqttService.disconnect()
        super.onTerminate()
    }
}
