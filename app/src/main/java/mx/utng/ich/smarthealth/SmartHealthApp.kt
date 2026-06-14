package mx.utng.ich.smarthealth

import android.app.Application
import mx.utng.ich.smarthealth.data.SmartHealthRepository

class SmartHealthApp : Application() {

    override fun onCreate() {
        super.onCreate()

        SmartHealthRepository.init(this)
    }
}