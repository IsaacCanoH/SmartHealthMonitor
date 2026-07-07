package mx.utng.smarthealthmonitor.tv

import android.app.Application
import mx.utng.smarthealthmonitor.data.SmartHealthRepository

class TvApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(this)
    }
}
