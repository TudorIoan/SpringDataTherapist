package app.springdata.therapist.model

enum class UserType(val roleName: String) {
    THERAPIST("therapist");

    companion object {
        fun fromString(userType: String): UserType {
            return UserType.entries.find { it.roleName == userType } ?: THERAPIST
        }

        fun toString(userType: UserType): String {
            return userType.roleName
        }

    }
}
