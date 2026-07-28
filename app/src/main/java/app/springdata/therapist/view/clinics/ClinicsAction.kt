package app.springdata.therapist.view.clinics

import app.springdata.therapist.model.Clinic

sealed class ClinicsAction {
    object Start : ClinicsAction()
    data class ClinicClicked(val clinic: Clinic) : ClinicsAction()
}
