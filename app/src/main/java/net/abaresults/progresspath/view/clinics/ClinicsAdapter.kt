package net.abaresults.progresspath.view.clinics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.abaresults.progresspath.R
import net.abaresults.progresspath.model.Clinic

class ClinicsAdapter(
    val onItemClicked: (Clinic) -> Unit
) :
    RecyclerView.Adapter<ClinicsAdapter.ViewHolder>() {

    private var items: MutableList<Clinic> = mutableListOf()

    fun updateData(newItems: List<Clinic>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.nameView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameView.text = items[position].name
        holder.itemView.setOnClickListener {
            onItemClicked(items[position])
        }
    }

    override fun getItemCount() = items.size
}
