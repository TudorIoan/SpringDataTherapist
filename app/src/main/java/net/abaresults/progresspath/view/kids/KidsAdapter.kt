package net.abaresults.progresspath.view.kids

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import net.abaresults.progresspath.databinding.ListItemBinding
import net.abaresults.progresspath.model.Kid

class KidsAdapter(
    val onItemClicked: (Kid) -> Unit,
    val onGenerateKidWorksheet: (Kid) -> Unit,
) :
    RecyclerView.Adapter<KidsAdapter.ViewHolder>() {

    private var items: MutableList<Kid> = mutableListOf()

    fun updateData(newItems: List<Kid>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.nameView.text = items[position].name

        // Hide buttons layout initially
        holder.binding.buttonsLayout.root.isVisible = false

        holder.binding.worksheetBtn.isVisible = true

        holder.binding.root.setOnClickListener {
            onItemClicked(items[position])
        }

        holder.binding.worksheetBtn.setOnClickListener {
            onGenerateKidWorksheet(items[position])
        }

    }

    override fun getItemCount() = items.size
}
