package com.example.googlesignin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MedAdapter(
    private var items: MutableList<Medicament>,
    private val onDelete: (Medicament)->Unit
) :RecyclerView.Adapter<MedAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imgInfo: ImageView = v.findViewById(R.id.imgInfo)
        val tvNom: TextView = v.findViewById(R.id.tvName)
        val tvInfos: TextView = v.findViewById(R.id.tvDetail)
        val tvRappel: TextView = v.findViewById(R.id.tv_horaire)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedAdapter.ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_medicament, parent, false)
    )

    override fun onBindViewHolder(holder: MedAdapter.ViewHolder, position: Int) {
        val m = items[position]
        holder.tvNom.text = m.nom
        holder.tvInfos.text = "${m.dose} ${m.type} • ${m.quantity} unités"
        // Formatage du rappel dans le style souhaité
        val rappelParts = m.rappel.split(", ") // Sépare "21/04/2024, 21:41"
        if (rappelParts.size == 2) {
            val time = rappelParts[1] // "21:41"

            // Formater l'affichage
            holder.tvRappel.text = "${time}"
        } else {
            holder.tvRappel.text = m.rappel
        }

        holder.imgInfo.setImageResource(
            if (m.isTaken) R.drawable.check else R.drawable.attention
        )

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, MedicationReminder::class.java).apply {
                putExtra("medicationId", m.id)
            }
            holder.itemView.context.startActivity(intent)
        }

        // Clic sur l'item complet pour ouvrir MedicationReminder
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, MedicationReminder::class.java).apply {
                putExtra("medicationId", m.id)
                putExtra("medicationName", m.nom)
                putExtra("medicationType", m.type)
                putExtra("medicationDose", m.dose)
                putExtra("medicationQuantity", m.quantity)
                putExtra("medicationReminder", m.rappel)
                putExtra("medicationIsTaken", m.isTaken)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            onDelete(m)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateMedications(newItems: List<Medicament>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }


    // Ajouter cette méthode pour mettre à jour un item spécifique
    fun updateItem(medicament: Medicament) {
        val position = items.indexOfFirst { it.id == medicament.id }
        if (position != -1) {
            items[position] = medicament
            notifyItemChanged(position)
        }
    }
}