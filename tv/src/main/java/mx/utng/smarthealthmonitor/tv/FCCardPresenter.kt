package mx.utng.smarthealthmonitor.tv

import android.graphics.Color
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import mx.utng.smarthealthmonitor.data.db.LecturaFC

class FCCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(240, 180)
        }

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val card = viewHolder.view as ImageCardView
        val lectura = item as LecturaFC

        card.titleText = "${lectura.valorBpm} bpm"
        card.contentText = lectura.hora

        val bgColor = if (lectura.esNormal) {
            Color.parseColor("#1B4F8A")
        } else {
            Color.parseColor("#B3261E")
        }

        card.setBackgroundColor(bgColor)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        (viewHolder.view as ImageCardView).mainImage = null
    }
}
