package mx.utng.smarthealthmonitor.tv

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.DetailsOverviewRow
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter
import androidx.leanback.widget.OnActionClickedListener
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.db.LecturaFC
import mx.utng.smarthealthmonitor.data.db.SmartHealthDB

class DetailFragment : DetailsSupportFragment(),
    OnActionClickedListener {

    companion object {
        const val ARG_LECTURA_ID = "lectura_id"
        const val ACTION_PLAY = 1L
        const val ACTION_BACK = 2L

        fun newInstance(lecturaId: Int): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().also {
                    it.putInt(ARG_LECTURA_ID, lecturaId)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getInt(ARG_LECTURA_ID) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val lectura = SmartHealthDB.getDatabase(requireContext())
                .lecturaDao()
                .obtenerPorId(id)

            lectura?.let { construirDetalle(it) }
        }
    }

    private fun construirDetalle(lectura: LecturaFC) {
        val selector = ClassPresenterSelector()

        val dpPresenter = FullWidthDetailsOverviewRowPresenter(
            DetailsDescriptionPresenter()
        )
        dpPresenter.setOnActionClickedListener(this)
        selector.addClassPresenter(DetailsOverviewRow::class.java, dpPresenter)

        val row = DetailsOverviewRow(lectura)
        val iconRes = if (lectura.esNormal) {
            android.R.drawable.ic_menu_compass
        } else {
            android.R.drawable.ic_dialog_alert
        }
        row.imageDrawable = ContextCompat.getDrawable(requireContext(), iconRes)

        val actions = ArrayObjectAdapter()
        actions.add(Action(ACTION_PLAY, "▶ Reproducir alerta"))
        actions.add(Action(ACTION_BACK, "← Volver al historial"))
        row.actionsAdapter = actions

        val adapter = ArrayObjectAdapter(selector)
        adapter.add(row)
        this.adapter = adapter
    }

    override fun onActionClicked(action: Action) {
        when (action.id) {
            ACTION_PLAY -> {
                Toast.makeText(context, "Reproducir", Toast.LENGTH_SHORT).show()
            }

            ACTION_BACK -> requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}
