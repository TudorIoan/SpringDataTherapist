package app.springdata.therapist.view.objectives

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import app.springdata.therapist.model.KidObjective
import app.springdata.therapist.model.Objective
import app.springdata.therapist.model.ObjectiveType
import app.springdata.therapist.model.UserType
import app.springdata.therapist.repo.KidObjectiveRepository
import app.springdata.therapist.repo.ObjectiveRepository
import app.springdata.therapist.repo.OrgRepository
import app.springdata.therapist.repo.UserRepository
import app.springdata.therapist.util.isItemAvailableForTherapist
import javax.inject.Inject

@HiltViewModel
class ObjectivesViewModel @Inject constructor(
    private val objectiveRepo: ObjectiveRepository,
    private val kidObjectiveRepo: KidObjectiveRepository,
    private val orgRepo: OrgRepository,
    private val userRepo: UserRepository
)  : ViewModel() {
    private val _state = MutableLiveData<ObjectivesState>()
        .apply { value = ObjectivesState.Idle }
    val state: LiveData<ObjectivesState> = _state

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _userType = MutableLiveData<UserType>()
    val userType: LiveData<UserType> = _userType

    private val _coordinatorName = MutableLiveData<String>()
    val coordinatorName: LiveData<String> = _coordinatorName

    val objectives = mutableListOf<Objective>()
    val kidObjectives = mutableListOf<KidObjective>()
    val objectivesListItems = mutableListOf<ObjectivesListItem>()
    private val expandedObjectiveTypes = mutableSetOf<ObjectiveType>()
    private var expandedStateKidId: String? = null

    fun takeAction(action: ObjectivesAction) {
        when (action) {
            is ObjectivesAction.Start -> handleStart()
            is ObjectivesAction.ObjectiveClicked -> handleObjectiveClicked(action.objectiveListItem)
        }
    }

    private fun update(newState: ObjectivesState) {
        _state.value = newState
    }

    private fun handleStart() {
        _title.value = "${orgRepo.requireSelectedClinic().name} > ${orgRepo.requireSelectedKid().name}"
        _userType.value = UserType.fromString(userRepo.requireUserDetails().userType)

        // Set selected kid to reset selected Objective
        orgRepo.setSelectedKid(orgRepo.requireSelectedKid())

        update(ObjectivesState.Loading)

        objectives.clear()
        kidObjectives.clear()
        objectivesListItems.clear()

        viewModelScope.launch {
            userRepo.fetchUserDetails(orgRepo.requireSelectedClinic().ownerUid)
                .onSuccess { _coordinatorName.value = it.name }

            val kidId = orgRepo.requireSelectedKid().id
            if (expandedStateKidId != kidId) {
                expandedObjectiveTypes.clear()
                expandedStateKidId = kidId
            }
            
            Log.d("ObjectivesViewModel", "Fetching active objectives for therapist. Kid ID: $kidId")
            
            val kidObjectivesResult = kidObjectiveRepo.getActiveKidObjectives(kidId)
            
            kidObjectivesResult.onSuccess { kidObjectivesList ->
                val currentUserId = userRepo.requireUserDetails().ownerUid
                val filteredKidObjectives = kidObjectivesList.filter { kidObjective ->
                    kidObjective.itemsList.any { item ->
                        isItemAvailableForTherapist(item, currentUserId)
                    }
                }.also { filtered ->
                    Log.d("ObjectivesViewModel", "Filtered ${kidObjectivesList.size} to ${filtered.size} objectives for therapist")
                }

                Log.d("ObjectivesViewModel", "Found ${filteredKidObjectives.size} kid objectives")
                kidObjectives.addAll(filteredKidObjectives)

                if (filteredKidObjectives.isNotEmpty()) {
                    // Get the actual objectives
                    val objectiveIds = filteredKidObjectives.map { it.objectiveId }
                    Log.d("ObjectivesViewModel", "Fetching objectives with IDs: $objectiveIds")
                    val objectivesResult = objectiveRepo.fetchObjectivesByIds(objectiveIds)
                    
                    objectivesResult.onSuccess { objectivesList ->
                        Log.d("ObjectivesViewModel", "Found ${objectivesList.size} objectives")
                        objectives.addAll(objectivesList)
                        rebuildObjectivesListItems()
                        update(ObjectivesState.ContentLoaded(objectivesListItems))
                    }.onFailure { exception ->
                        Log.e("ObjectivesViewModel", "Error fetching objectives", exception)
                        update(ObjectivesState.Error(exception.message ?: "Error fetching objectives"))
                    }
                } else {
                    update(ObjectivesState.ContentLoaded(objectivesListItems))
                }
            }.onFailure { exception ->
                update(ObjectivesState.Error(exception.message ?: "Error fetching kid objectives"))
            }
        }
    }

    private fun handleObjectiveClicked(objectivesListItem: ObjectivesListItem) {
        when (objectivesListItem) {
            is ObjectivesListItem.ObjectiveTypeItem -> {
                var itemIndex = objectivesListItems.indexOfFirst { it == objectivesListItem }
                val expanded = !objectivesListItem.expanded
                if (expanded) {
                    expandedObjectiveTypes.add(objectivesListItem.objectiveType)
                } else {
                    expandedObjectiveTypes.remove(objectivesListItem.objectiveType)
                }
                objectivesListItems[itemIndex] = objectivesListItem.copy(expanded = expanded)
                if ((objectivesListItems[itemIndex] as ObjectivesListItem.ObjectiveTypeItem).expanded) {
                    objectives.filter { it.type == objectivesListItem.objectiveType }.forEach { objective ->
                        itemIndex ++
                        val kidObjective = kidObjectives.find { it.objectiveId == objective.id }
                        if (kidObjective != null) {
                            objectivesListItems.add(itemIndex, ObjectivesListItem.ObjectiveItem(kidObjective, objective.name, objective.type))
                        }
                    }
                } else {
                    objectivesListItems.removeAll { it is ObjectivesListItem.ObjectiveItem && it.objectiveType == objectivesListItem.objectiveType }
                }
                update(ObjectivesState.ContentLoaded(objectivesListItems))
            }
            is ObjectivesListItem.ObjectiveItem -> {
                val objective = objectives.find { it.id == objectivesListItem.kidObjective.objectiveId }!!
                orgRepo.setSelectedObjective(objective, objectivesListItem.kidObjective)
                update(ObjectivesState.GoToItems)
                update(ObjectivesState.Idle)
            }
        }
    }

    private fun rebuildObjectivesListItems() {
        objectivesListItems.clear()
        objectives.map { it.type }.toSet().forEach { objectiveType ->
            val expanded = objectiveType in expandedObjectiveTypes
            objectivesListItems.add(ObjectivesListItem.ObjectiveTypeItem(objectiveType, expanded = expanded))
            if (expanded) {
                objectives.filter { it.type == objectiveType }.forEach { objective ->
                    val kidObjective = kidObjectives.find { it.objectiveId == objective.id }
                    if (kidObjective != null) {
                        objectivesListItems.add(ObjectivesListItem.ObjectiveItem(kidObjective, objective.name, objective.type))
                    }
                }
            }
        }
    }

}
