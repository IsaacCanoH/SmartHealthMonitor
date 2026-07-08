package mx.utng.smarthealthmonitor.tv

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import mx.utng.smarthealthmonitor.data.db.LecturaFC

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(
        viewHolder: ViewHolder,
        item: Any
    ) {
        val lectura = item as LecturaFC

        viewHolder.title.text = "${lectura.valorBpm} bpm"

        viewHolder.subtitle.text = if (lectura.esNormal) {
            "✓ Frecuencia normal"
        } else {
            "⚠ Fuera de rango — consulta al médico"
        }

        viewHolder.body.text =
            "Registrado a las ${lectura.hora}\n" +
                "ID de lectura: ${lectura.id}"
    }
}
