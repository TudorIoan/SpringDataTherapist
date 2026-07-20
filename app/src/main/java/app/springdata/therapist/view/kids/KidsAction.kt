package app.springdata.therapist.view.kids

import app.springdata.therapist.model.Kid

sealed class KidsAction {
    object Start : KidsAction()
    class KidClicked (val kid: Kid): KidsAction()
    class GenerateKidWorksheet(val kid: Kid): KidsAction()
}
