package app.springdata.therapist.view.kids

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.springdata.therapist.R
import app.springdata.therapist.model.Kid

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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.nameView)
        val initialsView: TextView = view.findViewById(R.id.initialsView)
        val worksheetBtn: ImageView = view.findViewById(R.id.worksheetBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.kid_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kid = items[position]
        holder.nameView.text = kid.name
        holder.initialsView.text = kidInitials(kid.name)
        holder.initialsView.background = kidInitialsBackground(holder.itemView)
        holder.initialsView.setTextColor(generatedInitialsColor(kid.name))
        holder.itemView.setOnClickListener {
            onItemClicked(kid)
        }

        holder.worksheetBtn.setOnClickListener {
            onGenerateKidWorksheet(kid)
        }

    }

    override fun getItemCount() = items.size

    private fun kidInitials(name: String): String {
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

    private fun kidInitialsBackground(view: View): GradientDrawable {
        val radius = view.resources.getDimension(R.dimen.icon_corner_radius)

        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(ContextCompat.getColor(view.context, R.color.itemInitialsBackground))
        }
    }

    private fun generatedInitialsColor(name: String): Int {
        val hue = (name.hashCode() and Int.MAX_VALUE) % 360
        return Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.76f, 0.76f))
    }
}
