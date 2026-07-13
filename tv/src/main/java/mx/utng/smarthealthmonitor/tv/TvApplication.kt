package mx.utng.smarthealthmonitor.tv

import android.app.Application
import mx.utng.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.smarthealthmonitor.data.TvDataReceiver

class TvApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(this)
        TvDataReceiver.iniciar()
    }

    override fun onTerminate() {
        TvDataReceiver.detener()
        super.onTerminate()
    }
}
