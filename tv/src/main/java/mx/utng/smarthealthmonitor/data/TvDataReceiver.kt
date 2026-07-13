package mx.utng.smarthealthmonitor.data

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.ServerSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Recibe del móvil lecturas de FC en la red local y las persiste en Room. */
object TvDataReceiver {

    private const val PORT = 45888
    private const val PREFIX = "SMARTHEALTH_FC:"
    private const val TAG = "TvDataReceiver"

    private var scope: CoroutineScope? = null
    private var socket: DatagramSocket? = null
    private var serverSocket: ServerSocket? = null
    private var receiverJob: Job? = null
    private var tcpJob: Job? = null

    fun iniciar() {
        if (receiverJob?.isActive == true) return

        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope = newScope
        receiverJob = newScope.launch {
            runCatching {
                DatagramSocket(PORT).use { receiver ->
                    socket = receiver
                    Log.d(TAG, "Escuchando datos del móvil en puerto $PORT")

                    while (isActive) {
                        val buffer = ByteArray(128)
                        val packet = DatagramPacket(buffer, buffer.size)
                        receiver.receive(packet)

                        val message = String(packet.data, 0, packet.length, Charsets.UTF_8)
                        val bpm = message.takeIf { it.startsWith(PREFIX) }
                            ?.removePrefix(PREFIX)
                            ?.toIntOrNull()

                        if (bpm != null && bpm in 20..250) {
                            SmartHealthRepository.actualizarFC(bpm)
                            Log.d(TAG, "FC recibida y guardada: $bpm")
                        }
                    }
                }
            }.onFailure { error ->
                if (isActive) Log.e(TAG, "Error recibiendo datos del móvil", error)
            }
            socket = null
        }

        tcpJob = newScope.launch {
            runCatching {
                ServerSocket(PORT).use { server ->
                    serverSocket = server
                    Log.d(TAG, "Escuchando emulador móvil por TCP en puerto $PORT")

                    while (isActive) {
                        server.accept().use { client ->
                            val message = client.getInputStream()
                                .bufferedReader(Charsets.UTF_8)
                                .readText()
                            guardarMensaje(message)
                        }
                    }
                }
            }.onFailure { error ->
                if (isActive) Log.e(TAG, "Error recibiendo datos TCP", error)
            }
            serverSocket = null
        }
    }

    private suspend fun guardarMensaje(message: String) {
        val bpm = message.takeIf { it.startsWith(PREFIX) }
            ?.removePrefix(PREFIX)
            ?.toIntOrNull()

        if (bpm != null && bpm in 20..250) {
            SmartHealthRepository.actualizarFC(bpm)
            Log.d(TAG, "FC recibida y guardada: $bpm")
        }
    }

    fun detener() {
        socket?.close()
        socket = null
        serverSocket?.close()
        serverSocket = null
        scope?.cancel()
        scope = null
        receiverJob = null
    }
}
