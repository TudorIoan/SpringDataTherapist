package app.springdata.therapist.view.chart

import app.springdata.therapist.model.KidObjectiveItem

sealed class ChartState {
    object Idle : ChartState()
    class ContentLoaded(val items: List<KidObjectiveItem>) : ChartState()
}
