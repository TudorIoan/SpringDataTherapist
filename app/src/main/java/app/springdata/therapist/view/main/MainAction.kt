package app.springdata.therapist.view.main

sealed class MainAction {
    object Start : MainAction()
    object UpdateAuthStatus: MainAction()
}