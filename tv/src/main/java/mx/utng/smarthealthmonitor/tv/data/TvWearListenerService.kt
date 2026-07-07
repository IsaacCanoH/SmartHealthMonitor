package mx.utng.smarthealthmonitor.tv.data

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.SmartHealthRepository

class TvWearListenerService : WearableListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(applicationContext)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val data = String(messageEvent.data)

        Log.d(TAG, "Mensaje recibido: path=${messageEvent.path}, data=$data")

        when (messageEvent.path) {
            PATH_FC -> {
                val bpm = data.toIntOrNull() ?: return

                scope.launch {
                    SmartHealthRepository.actualizarFC(bpm)
                    Log.d(TAG, "FC guardada en Room TV: $bpm")
                }
            }

            else -> Log.w(TAG, "Path desconocido: ${messageEvent.path}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val PATH_FC = "/smarthealthmonitor/fc"
        private const val TAG = "TvWearListener"
    }
}
