package mx.utng.smarthealthmonitor.tv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.db.LecturaFC

class MainFragment : BrowseSupportFragment() {

    private val viewModel: TvViewModel by viewModels()
    private lateinit var histAdapter: ArrayObjectAdapter
    private lateinit var neonAdapter: ArrayObjectAdapter
    private lateinit var statsAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = "SmartHealth TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = resources.getColor(R.color.sh_primary, null)

        cargarFilas()
        configurarClickLectura()
        observarDatos()
    }

    private fun configurarClickLectura() {
        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is LecturaFC) {
                val detail = DetailFragment.newInstance(item.id)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_browse_fragment, detail)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun observarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.historial.collect { lecturas ->
                        histAdapter.clear()
                        lecturas.forEach { histAdapter.add(it) }
                    }
                }
                launch {
                    viewModel.state.collect { state ->
                        neonAdapter.clear()
                        state.lecturasNeon.forEach { neonAdapter.add(it) }
                        statsAdapter.clear()
                        state.estadisticas.forEach { statsAdapter.add(it) }
                    }
                }
            }
        }
    }

    private fun cargarFilas() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        histAdapter = ArrayObjectAdapter(FCCardPresenter())
        neonAdapter = ArrayObjectAdapter(NeonFcCardPresenter())
        statsAdapter = ArrayObjectAdapter(NeonFcCardPresenter())
        rowsAdapter.add(ListRow(HeaderItem("Historial local"), histAdapter))
        rowsAdapter.add(ListRow(HeaderItem("Historial Neon"), neonAdapter))
        rowsAdapter.add(ListRow(HeaderItem("Promedios por dispositivo"), statsAdapter))

        adapter = rowsAdapter
    }
}
