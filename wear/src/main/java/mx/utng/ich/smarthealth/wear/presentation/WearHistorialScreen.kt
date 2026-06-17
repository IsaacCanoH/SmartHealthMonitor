package mx.utng.ich.smarthealth.wear.presentation

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import kotlinx.coroutines.launch
import mx.utng.ich.smarthealth.wear.presentation.components.WearFilaHistorial

@Composable
fun WearHistorialScreen(
    onBack: () -> Unit,
    viewModel: WearDashboardViewModel = viewModel()
) {
    val historial by viewModel.historial.collectAsState()
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        timeText = {
            TimeText(
                modifier = Modifier.scrollAway(listState)
            )
        },
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState
            )
        }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .onRotaryScrollEvent { event ->

                    val totalItems = if (historial.isEmpty()) {
                        2
                    } else {
                        historial.size + 1
                    }

                    val maxIndex = (totalItems - 1).coerceAtLeast(0)

                    val targetIndex = if (event.verticalScrollPixels > 0) {
                        (listState.centerItemIndex + 1).coerceAtMost(maxIndex)
                    } else {
                        (listState.centerItemIndex - 1).coerceAtLeast(0)
                    }

                    scope.launch {
                        listState.animateScrollToItem(targetIndex)
                    }

                    true
                }
                .focusRequester(focusRequester)
                .focusable()
        ) {
            item {
                Text(
                    text = "Historial (${historial.size})",
                    style = MaterialTheme.typography.title3,
                    modifier = Modifier.padding(8.dp)
                )
            }

            if (historial.isEmpty()) {
                item {
                    Text(
                        text = "Sin lecturas aún",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                items(
                    count = historial.size,
                    key = { index ->
                        historial[index].id
                    }
                ) { index ->
                    WearFilaHistorial(
                        lectura = historial[index]
                    )
                }
            }
        }
    }
}