package app.springdata.therapist.view.items

import app.springdata.therapist.model.KidObjectiveItem
import app.springdata.therapist.model.ObjItem

sealed class ItemAction {
    object Start : ItemAction()
    class ItemNoClicked (val item: KidObjectiveItem): ItemAction()
    class ItemYesClicked (val item: KidObjectiveItem): ItemAction()
    class FrequencySet (val item: KidObjectiveItem, val frequency: Int): ItemAction()
    class ProgressSet (val item: KidObjectiveItem, val progress: Int): ItemAction()
    class ItemToggleClicked (val item: KidObjectiveItem): ItemAction()
    class SetMastered (val item: KidObjectiveItem): ItemAction()
    class CheckmarkClicked (val item: KidObjectiveItem): ItemAction()
    data class RemoveItem(val item: KidObjectiveItem) : ItemAction()
}