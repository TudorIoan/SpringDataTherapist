package app.springdata.therapist.view.objectives

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.springdata.therapist.databinding.ObjectiveItemBinding
import app.springdata.therapist.databinding.ObjectiveTypeItemBinding
import app.springdata.therapist.model.KidObjective
import app.springdata.therapist.model.ObjLevel
import app.springdata.therapist.model.ObjectiveType

sealed class ObjectivesListItem {
    data class ObjectiveTypeItem(val objectiveType: ObjectiveType, val expanded: Boolean = false) : ObjectivesListItem()
    data class ObjectiveItem(val kidObjective: KidObjective, val objectiveName: String, val objectiveType: ObjectiveType) : ObjectivesListItem()
}

class ObjectivesAdapter(
    val onItemClicked: (ObjectivesListItem) -> Unit
) : RecyclerView.Adapter<ObjectivesAdapter.BaseViewHolder<*>>() {

    private var items: List<ObjectivesListItem> = emptyList()

    fun set(newItems: List<ObjectivesListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    class ObjectiveTypeViewHolder(
        private val binding: ObjectiveTypeItemBinding,
        private val onItemClicked: (ObjectivesListItem.ObjectiveTypeItem) -> Unit,
    ) : BaseViewHolder<ObjectivesListItem.ObjectiveTypeItem>(binding.root) {

        override fun bind(item: ObjectivesListItem.ObjectiveTypeItem) {
            binding.textView.text = item.objectiveType.displayName

            // Set level label text and background color
            binding.levelLabel.text = item.objectiveType.level.displayName.uppercase()
            binding.levelLabel.isVisible = true

            // Create rounded background with appropriate color
            val backgroundDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f
                setColor(getLevelBackgroundColor(item.objectiveType.level))
            }
            binding.levelLabel.background = backgroundDrawable

            binding.root.setOnClickListener {
                onItemClicked(item)
            }
        }

        private fun getLevelBackgroundColor(level: ObjLevel): Int {
            return when (level) {
                ObjLevel.BEGINNER -> 0xFF4CAF50.toInt() // Green
                ObjLevel.INTERMEDIATE -> 0xFFe3dc12.toInt() // Darker shade of yellow
                ObjLevel.ADVANCED -> 0xFF2196F3.toInt() // Blue
            }
        }
    }

    class ObjectiveViewHolder(
        private val binding: ObjectiveItemBinding,
        private val onItemClicked: (ObjectivesListItem.ObjectiveItem) -> Unit
    ) : BaseViewHolder<ObjectivesListItem.ObjectiveItem>(binding.root) {

        override fun bind(item: ObjectivesListItem.ObjectiveItem) {
            binding.nameView.text = item.objectiveName

            binding.activeToggle.isVisible = false
            binding.objectiveReportView.isVisible = false
            binding.buttonsLayout.root.isVisible = false

            binding.root.setOnClickListener {
                onItemClicked(item)
            }

            if (item.kidObjective.active && item.kidObjective.itemsList.all { it.mastered }) {
                binding.root.alpha = 0.5f
            } else {
                binding.root.alpha = 1.0f
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ObjectivesListItem.ObjectiveTypeItem -> VIEW_TYPE_TYPE
            is ObjectivesListItem.ObjectiveItem -> VIEW_TYPE_OBJECTIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TYPE -> {
                val binding = ObjectiveTypeItemBinding.inflate(inflater, parent, false)
                ObjectiveTypeViewHolder(binding, onItemClicked)
            }
            VIEW_TYPE_OBJECTIVE -> {
                val binding = ObjectiveItemBinding.inflate(inflater, parent, false)
                ObjectiveViewHolder(binding, onItemClicked)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = items[position]
        when (holder) {
            is ObjectiveTypeViewHolder -> holder.bind(item as ObjectivesListItem.ObjectiveTypeItem)
            is ObjectiveViewHolder -> holder.bind(item as ObjectivesListItem.ObjectiveItem)
        }
    }

    override fun getItemCount() = items.size

    companion object {
        private const val VIEW_TYPE_TYPE = 1
        private const val VIEW_TYPE_OBJECTIVE = 2
    }
}
