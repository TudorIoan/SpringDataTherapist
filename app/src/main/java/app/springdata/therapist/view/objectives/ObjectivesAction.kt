package app.springdata.therapist.view.objectives

import app.springdata.therapist.model.KidObjective

sealed class ObjectivesAction {
    object Start : ObjectivesAction()
    class ObjectiveClicked(val objectiveListItem: ObjectivesListItem) : ObjectivesAction()
}
