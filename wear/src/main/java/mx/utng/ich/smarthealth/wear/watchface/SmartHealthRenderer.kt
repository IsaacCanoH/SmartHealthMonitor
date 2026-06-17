package mx.utng.ich.smarthealth.wear.watchface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import java.time.ZonedDateTime
import mx.utng.ich.smarthealth.wear.data.WearHealthRepository

class SmartHealthRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    interactiveDrawModeUpdateDelayMillis: Long
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = CanvasType.HARDWARE,
    interactiveDrawModeUpdateDelayMillis = interactiveDrawModeUpdateDelayMillis,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false
) {

    private val paintHora = Paint().apply {
        color = Color.WHITE
        textSize = 72f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    private val paintFC = Paint().apply {
        color = Color.RED
        textSize = 30f
        isAntiAlias = true
    }

    private val paintSub = Paint().apply {
        color = Color.GRAY
        textSize = 22f
        isAntiAlias = true
    }

    override suspend fun createSharedAssets(): Renderer.SharedAssets =
        object : Renderer.SharedAssets {
            override fun onDestroy() {
                // No hay recursos compartidos que liberar por ahora
            }
        }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: Renderer.SharedAssets
    ) {
        // Fondo negro — ahorra batería en modo AOD
        canvas.drawColor(Color.BLACK)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()

        // Hora digital centrada
        val hora = String.format(
            "%02d:%02d",
            zonedDateTime.hour,
            zonedDateTime.minute
        )

        val anchoHora = paintHora.measureText(hora)

        canvas.drawText(
            hora,
            cx - anchoHora / 2,
            cy - 10f,
            paintHora
        )

        // Segundos pequeños debajo
        val segundos = String.format(
            "%02d",
            zonedDateTime.second
        )

        val anchoSegundos = paintSub.measureText(segundos)

        canvas.drawText(
            segundos,
            cx - anchoSegundos / 2,
            cy + 30f,
            paintSub
        )

        // FC desde el repository local del módulo wear
        val fc = WearHealthRepository.fcFlow.value

        if (fc > 0) {
            val fcTexto = "❤ $fc bpm"
            val anchoFC = paintFC.measureText(fcTexto)

            canvas.drawText(
                fcTexto,
                cx - anchoFC / 2,
                cy + 70f,
                paintFC
            )
        }
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: Renderer.SharedAssets
    ) {
        renderParameters.highlightLayer?.let { highlightLayer ->
            canvas.drawColor(highlightLayer.backgroundTint)
        }
    }
}