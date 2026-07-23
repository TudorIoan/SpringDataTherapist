package app.springdata.therapist.view.clinics

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.springdata.therapist.R
import app.springdata.therapist.model.Clinic

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
        val initialsView: TextView = view.findViewById(R.id.initialsView)
        val metaView: TextView = view.findViewById(R.id.metaView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.clinic_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clinic = items[position]
        holder.nameView.text = clinic.name
        holder.initialsView.text = clinicInitials(clinic.name)
        holder.initialsView.background = clinicInitialsBackground(clinic.name)
        holder.metaView.text = "${clinic.kidsCount} ${if (clinic.kidsCount == 1) "KID" else "KIDS"}"
        holder.itemView.setOnClickListener {
            onItemClicked(clinic)
        }
    }

    override fun getItemCount() = items.size

    private fun clinicInitials(name: String): String {
        val words = name
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}"
            words.size == 1 -> words[0].take(2)
            else -> "?"
        }.uppercase()
    }

    private fun clinicInitialsBackground(name: String): GradientDrawable {
        val hue = (name.hashCode() and Int.MAX_VALUE) % 360
        val color = Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.74f, 0.94f))
        val radius = 10f * android.content.res.Resources.getSystem().displayMetrics.density

        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(color)
        }
    }
}
