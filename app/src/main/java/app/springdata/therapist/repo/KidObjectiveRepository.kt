package app.springdata.therapist.repo

import app.springdata.therapist.model.KidObjective
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KidObjectiveRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) {

    suspend fun getActiveKidObjectives(kidId: String): Result<List<KidObjective>> {
        if (kidId.isBlank()) {
            return Result.failure(Exception("Kid ID cannot be empty."))
        }

        return try {
            val querySnapshot = firestore.collection("kid_objectives")
                .whereEqualTo("kidId", kidId)
                .whereEqualTo("active", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val kidObjectives = querySnapshot.documents.mapNotNull { document ->
                document.toObject(KidObjective::class.java)
            }

            Result.success(kidObjectives.map { populateUserNamesForItems(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateKidObjective(kidObjective: KidObjective): Result<Unit> {
        if (kidObjective.id.isBlank()) {
            return Result.failure(Exception("Kid Objective ID cannot be empty."))
        }

        return try {
            firestore.collection("kid_objectives")
                .document(kidObjective.id)
                .set(kidObjective)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun populateUserNamesForItems(kidObjective: KidObjective): KidObjective {
        val updatedItemsList = kidObjective.itemsList.map { item ->
            item.lastModificationByUserName = item.lastModificationByUserId?.let { userId ->
                userRepository.therapistUsers.find { it.ownerUid == userId }?.name
            }
            item
        }
        return kidObjective.copy(itemsList = updatedItemsList)
    }
}
