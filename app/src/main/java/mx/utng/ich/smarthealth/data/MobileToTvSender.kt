package mx.utng.ich.smarthealth.data

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/** Reenvía a las TV de la red local las lecturas recibidas desde Wear OS. */
object MobileToTvSender {

    private const val PORT = 45888
    private const val TAG = "MobileToTvSender"

    fun enviarFC(bpm: Int) {
        val data = "SMARTHEALTH_FC:$bpm".toByteArray(Charsets.UTF_8)

        // En los emuladores, 10.0.2.2 apunta al equipo anfitrión. Un adb forward
        // lleva esta conexión hasta el puerto TCP de la TV virtual.
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("10.0.2.2", PORT), 1_000)
                socket.getOutputStream().use { it.write(data) }
            }
        }.onSuccess {
            Log.d(TAG, "FC reenviada a TV virtual: $bpm")
        }.onFailure {
            Log.d(TAG, "No hay TV virtual conectada; se intentará por red local")
        }

        runCatching {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                val packet = DatagramPacket(
                    data,
                    data.size,
                    InetAddress.getByName("255.255.255.255"),
                    PORT
                )
                socket.send(packet)
            }
        }.onSuccess {
            Log.d(TAG, "FC reenviada a TV: $bpm")
        }.onFailure { error ->
            Log.e(TAG, "No se pudo reenviar la FC a TV", error)
        }
    }
}
