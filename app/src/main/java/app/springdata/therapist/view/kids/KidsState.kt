package app.springdata.therapist.view.kids

import app.springdata.therapist.model.Kid
import app.springdata.therapist.model.UserType

sealed class KidsState {
        object Idle: KidsState()
        object Loading : KidsState()
        class ContentLoaded(val items: List<Kid>, val userType: UserType) : KidsState()
        class Error(val generalError: String) : KidsState()
        object GoToObjectives : KidsState()
        class GoToReport(val pdfData: ByteArray) : KidsState()
}
