package app.springdata.therapist.view.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.springdata.therapist.databinding.ItemItemBinding
import app.springdata.therapist.model.KidObjectiveItem
import app.springdata.therapist.model.ObjItemType

class ItemsAdapter(
    private val onToggleClicked: (KidObjectiveItem) -> Unit,
    private val onYesClicked: (KidObjectiveItem) -> Unit,
    private val onNoClicked: (KidObjectiveItem) -> Unit,
    private val onFrequencySet: (KidObjectiveItem, Int) -> Unit,
    private val onProgressSet: (KidObjectiveItem, Int) -> Unit,
    private val onCheckmarkClicked: (KidObjectiveItem) -> Unit
) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    private var items: MutableList<KidObjectiveItem> = mutableListOf()

    fun updateData(newItems: List<KidObjectiveItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.itemNameView.text = item.objItem.name

        holder.binding.itemTypeView.visibility = View.GONE

        val isYesNoItem = item.objItem.type == ObjItemType.YES_NO
        val isFrequencyItem = item.objItem.type == ObjItemType.FREQUENCY
        val isCheckmarkItem = item.objItem.type == ObjItemType.CHECKMARK
        val isPercentageItem = item.objItem.type == ObjItemType.PERCENTAGE

        holder.binding.yesNoButtonsLayout.visibility = if (item.active && !item.mastered && isYesNoItem) View.VISIBLE else View.GONE
        holder.binding.frequencyInputLayout.visibility = if (item.active && !item.mastered && isFrequencyItem) View.VISIBLE else View.GONE
        holder.binding.percentageInputLayout.visibility = if (item.active && !item.mastered && isPercentageItem) View.VISIBLE else View.GONE
        holder.binding.checkmarkYesButton.visibility = if (item.active && !item.mastered && isCheckmarkItem) View.VISIBLE else View.GONE
        holder.binding.switchToggle.visibility = if (!item.active && !item.mastered) View.VISIBLE else View.GONE
        holder.binding.masteredImageView.visibility = if (item.mastered) View.VISIBLE else View.GONE

        holder.binding.switchToggle.setOnCheckedChangeListener(null) // Prevent triggering listener
        holder.binding.switchToggle.isChecked = item.active

        holder.binding.lastModificationView.visibility = View.GONE
        holder.binding.therapistView.visibility = View.GONE

        holder.binding.yesButton.setOnClickListener {
            onYesClicked(item)
        }

        holder.binding.noButton.setOnClickListener {
            onNoClicked(item)
        }

        holder.binding.switchToggle.setOnCheckedChangeListener { _, isChecked ->
            item.active = isChecked
            onToggleClicked(item)
        }

        holder.binding.setButton.setOnClickListener {
            val frequencyText = holder.binding.frequencyEditText.text.toString()
            val frequency = frequencyText.toIntOrNull() ?: 0
            onFrequencySet(item, frequency)
        }

        holder.binding.setPercentageButton.setOnClickListener {
            val progressText = holder.binding.percentageEditText.text.toString()
            val progress = progressText.toIntOrNull()?.coerceIn(0, 100) ?: 0
            onProgressSet(item, progress)
        }

        holder.binding.checkmarkYesButton.setOnClickListener {
            onCheckmarkClicked(item)
        }

    }

    override fun getItemCount() = items.size
}
