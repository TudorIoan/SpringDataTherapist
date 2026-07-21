package app.springdata.therapist.repo

import android.util.Log
import app.springdata.therapist.model.Clinic
import app.springdata.therapist.model.Kid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClinicRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun fetchAllClinicsWithCurrentTherapist(): Result<List<Clinic>> {
        val currentUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("User not authenticated for therapist check"))

        return try {
            val kidsQuerySnapshot = firestore.collection("kids")
                .whereArrayContains("therapistList", currentUser.uid)
                .get()
                .await()

            if (kidsQuerySnapshot.isEmpty) {
                return Result.success(emptyList())
            }

            val clinicIds = kidsQuerySnapshot.documents
                .mapNotNull { it.toObject(Kid::class.java)?.clinicId }
                .filter { it.isNotBlank() }
                .distinct()

            if (clinicIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val clinics = mutableListOf<Clinic>()
            val clinicIdChunks = clinicIds.chunked(30)

            for (chunk in clinicIdChunks) {
                val clinicsQuerySnapshot = firestore.collection("clinics")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()

                clinicsQuerySnapshot.documents.mapNotNullTo(clinics) { document ->
                    document.toObject(Clinic::class.java)
                }
            }

            Result.success(clinics.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Log.e("ClinicRepo", "Error fetching clinics for therapist ${currentUser.uid}: ${e.message}", e)
            Result.failure(e)
        }
    }
}
