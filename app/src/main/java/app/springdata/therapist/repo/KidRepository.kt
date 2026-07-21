package app.springdata.therapist.repo

import app.springdata.therapist.model.Kid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KidRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun getKidById(kidId: String): Result<Kid?> {
        if (kidId.isBlank()) {
            return Result.failure(IllegalArgumentException("Kid ID cannot be empty."))
        }
        return try {
            val documentSnapshot = firestore.collection("kids").document(kidId).get().await()
            if (documentSnapshot.exists()) {
                val kid = documentSnapshot.toObject(Kid::class.java)
                if (kid != null) {
                    Result.success(kid)
                } else {
                    Result.failure(Exception("Kid document exists but could not be converted to Kid object for ID: $kidId"))
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllKidsForClinicAndTherapist(clinicId: String, therapistId: String): Result<List<Kid>> {
        if (clinicId.isBlank()) {
            return Result.failure(Exception("Clinic ID cannot be empty."))
        }
        if (therapistId.isBlank()) {
            return Result.failure(Exception("Therapist ID cannot be empty."))
        }

        return try {
            val querySnapshot = firestore.collection("kids")
                .whereEqualTo("clinicId", clinicId)
                .whereArrayContains("therapistList", therapistId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val kids = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Kid::class.java)
            }
            Result.success(kids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateKidTherapistList(kid: Kid): Result<Unit> {
        val currentUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("User not authenticated"))

        if (kid.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Kid ID cannot be empty."))
        }

        return try {
            val kidRef = firestore.collection("kids").document(kid.id)
            val currentKidDoc = kidRef.get().await()

            if (!currentKidDoc.exists()) {
                return Result.failure(Exception("Kid with ID '${kid.id}' not found."))
            }

            kidRef.update("therapistList", kid.therapistList).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
