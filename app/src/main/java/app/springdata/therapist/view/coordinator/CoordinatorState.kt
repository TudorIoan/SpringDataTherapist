package app.springdata.therapist.view.coordinator

import app.springdata.therapist.model.UserProfile

sealed class CoordinatorState {
    object Idle : CoordinatorState()
    object Loading : CoordinatorState()
    class ContentLoaded(val coordinatorUser: UserProfile) : CoordinatorState()
    class Error(val generalError: String) : CoordinatorState()
}
