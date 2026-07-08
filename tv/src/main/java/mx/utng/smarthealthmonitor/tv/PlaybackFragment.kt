package mx.utng.smarthealthmonitor.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.PlaybackSupportFragment
import androidx.leanback.app.PlaybackSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.leanback.LeanbackPlayerAdapter

class PlaybackFragment : PlaybackSupportFragment() {

    private lateinit var player: ExoPlayer
    private var playerGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>? = null

    companion object {
        private const val UPDATE_DELAY_MS = 16
        const val ARG_URL = "media_url"
        const val ARG_TITLE = "media_title"

        fun newInstance(url: String, title: String = "Alerta"): PlaybackFragment {
            return PlaybackFragment().apply {
                arguments = Bundle().also {
                    it.putString(ARG_URL, url)
                    it.putString(ARG_TITLE, title)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString(ARG_URL) ?: return
        val mediaTitle = arguments?.getString(ARG_TITLE) ?: ""

        player = ExoPlayer.Builder(requireContext()).build()

        val adapter = LeanbackPlayerAdapter(
            requireContext(),
            player,
            UPDATE_DELAY_MS
        )
        playerGlue = PlaybackTransportControlGlue(requireContext(), adapter).apply {
            title = mediaTitle
            subtitle = "SmartHealth Monitor"
            host = PlaybackSupportFragmentGlueHost(this@PlaybackFragment)
            playWhenPrepared()
        }

        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::player.isInitialized) {
            player.release()
        }
        playerGlue = null
    }
}
