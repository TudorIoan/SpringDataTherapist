package app.springdata.therapist.view.main

import app.springdata.therapist.model.UserType

sealed class MainState {
        object Idle: MainState()
        data class AuthStatus(val isLoggedIn: Boolean, val userType: UserType? = null) : MainState()
}