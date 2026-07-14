package mx.utng.smarthealthmonitor.tv

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import mx.utng.smarthealthmonitor.tv.data.TvNeonItem

class NeonFcCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setCardType(ImageCardView.CARD_TYPE_INFO_UNDER_WITH_EXTRA)
            setInfoVisibility(ImageCardView.CARD_REGION_VISIBLE_ALWAYS)
            setMainImageDimensions(240, 180)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val lectura = item as? TvNeonItem ?: return
        val card = viewHolder.view as ImageCardView

        card.titleText = "${lectura.bpm} bpm · ${lectura.dispositivo}"
        card.contentText = if (lectura.esEstadistica) {
            "${lectura.estado} · última ${lectura.hora}"
        } else {
            "${lectura.estado} · ${lectura.hora}"
        }

        val color = when {
            lectura.esEstadistica -> Color.parseColor("#4A148C")
            lectura.bpm in 60..100 -> Color.parseColor("#1B4F8A")
            else -> Color.parseColor("#B3261E")
        }
        card.mainImage = ColorDrawable(color)
        card.setBackgroundColor(color)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        (viewHolder.view as ImageCardView).mainImage = null
    }
}
