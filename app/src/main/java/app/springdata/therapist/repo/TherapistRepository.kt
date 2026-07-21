package app.springdata.therapist.repo

import app.springdata.therapist.model.InvitedTherapist
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TherapistRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun isTherapistInvited(email: String): Result<String?> {
        val therapistEmail = email.trim().lowercase()
        if (therapistEmail.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty."))
        }

        return try {
            val invitesQuerySnapshot = firestore.collection("therapist_invites")
                .whereEqualTo("email", therapistEmail)
                .limit(1)
                .get()
                .await()

            if (invitesQuerySnapshot.isEmpty) {
                Result.success(null)
            } else {
                val invitedTherapist = invitesQuerySnapshot.documents
                    .first()
                    .toObject(InvitedTherapist::class.java)
                Result.success(invitedTherapist?.coordinatorName)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to check invitation status: ${e.localizedMessage}", e))
        }
    }
}
