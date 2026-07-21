package app.springdata.therapist.repo

import app.springdata.therapist.model.Objective
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObjectiveRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun fetchObjectivesByIds(objectiveIds: List<String>): Result<List<Objective>> {
        val uniqueObjectiveIds = objectiveIds.distinct()
        if (uniqueObjectiveIds.isEmpty()) {
            return Result.success(emptyList())
        }

        return try {
            val objectivesById = mutableMapOf<String, Objective>()
            uniqueObjectiveIds.chunked(30).forEach { chunk ->
                val querySnapshot = firestore.collection("objectives")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()

                querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Objective::class.java)
                }.forEach { objective ->
                    objectivesById[objective.id] = objective
                }
            }

            Result.success(uniqueObjectiveIds.mapNotNull { objectivesById[it] })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
