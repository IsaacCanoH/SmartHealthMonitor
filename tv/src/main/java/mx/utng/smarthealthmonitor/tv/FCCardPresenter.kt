package mx.utng.smarthealthmonitor.tv

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import mx.utng.smarthealthmonitor.data.db.LecturaFC

class FCCardPresenter : Presenter() {

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
        val card = viewHolder.view as ImageCardView
        val lectura = item as? LecturaFC ?: return

        card.titleText = "${lectura.valorBpm} bpm"
        card.contentText = lectura.hora

        val bgColor = if (lectura.esNormal) {
            Color.parseColor("#1B4F8A")
        } else {
            Color.parseColor("#B3261E")
        }

        card.mainImage = ColorDrawable(bgColor)
        card.setBackgroundColor(bgColor)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        (viewHolder.view as ImageCardView).mainImage = null
    }
}
