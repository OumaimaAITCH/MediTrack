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
) :RecyclerView.Adapter<MedAdapter.ViewHolder>(){

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val imgInfo : ImageView = v.findViewById(R.id.imgInfo)
        val tvNom   : TextView = v.findViewById(R.id.tvName)
        val tvInfos : TextView = v.findViewById(R.id.tvDetail)
        val tvRappel: TextView = v.findViewById(R.id.tv_horaire)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedAdapter.ViewHolder =ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_medicament, parent, false)
    )

    override fun onBindViewHolder(holder: MedAdapter.ViewHolder, position: Int) {
        val m = items[position]
        holder.tvNom.text = m.nom

        // Formatage du rappel dans le style souhaité
        val rappelParts = m.rappel.split(", ") // Sépare "21/04/2024, 21:41"
        if (rappelParts.size == 2) {
            val dateParts = rappelParts[0].split("/") // Sépare "21/04/2024"
            val time = rappelParts[1] // "21:41"

            // Convertir en Calendar pour obtenir le jour de la semaine
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                set(Calendar.MONTH, dateParts[1].toInt() - 1)
                set(Calendar.YEAR, dateParts[2].toInt())
            }

            // Obtenir le jour en français
            val joursSemaine = arrayOf("dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi")
            val jour = joursSemaine[calendar.get(Calendar.DAY_OF_WEEK) - 1]

            // Formater l'affichage
            holder.tvRappel.text = "Prévu pour ${time}, ${jour}"
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


//        holder.findViewById<LinearLayoutManager>(R.id.itemMedicament).setOnClickListener {
//            val intent = Intent(this, MedicationReminder::class.java)
//            intent.putExtra("medicationId", it.id)
//            startActivity(intent)
//        }
        holder.itemView.setOnLongClickListener {
            onDelete(m)
            true
        }
//        holder.imgInfo.setOnClickListener {
//                m.isTaken = !m.isTaken
//                // soit on rebinde directement :
//                holder.imgInfo.setImageResource(
//                    if (m.isTaken) R.drawable.check
//                    else R.drawable.attention
//                )
////                 ou bien on notifie l’item pour redessiner tout
//                 notifyItemChanged(position)
//            }
    }

    override fun getItemCount(): Int = items.size

    fun updateMedications(newItems: List<Medicament>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

//    fun add(m: Medicament) {
//        items.add(m)
//        notifyItemInserted(items.size - 1)
//    }

//    fun remove(m: Medicament) {
//        val position = items.indexOf(m)
//        if (position != -1) {
//            items.removeAt(position)
//            notifyItemRemoved(position)
//        }
//    }

}
//package com.example.googlesignin
//
//import android.annotation.SuppressLint
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class MedicamentAdapter (
//    private val items: List<T>,
//    private val onDeleteClick: (Medicament) -> Unit
//    ) : RecyclerView.Adapter<MedicamentAdapter.ViewHolder>() {
//
//    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
//        private val imgInfo = v.findViewById<ImageView>(R.id.imgInfo)
//        private val name = v.findViewById<TextView>(R.id.tvName)
//        private val detail = v.findViewById<TextView>(R.id.tvDetail)
//        private val time = v.findViewById<TextView>(R.id.tvTime)
//
//        @SuppressLint("SetTextI18n")
//        fun bind(m: Medicament) {
//            // Texte
//            name.text = m.nom
//            detail.text = m.dose + " • " + m.quantite + " unités • " + m.type
//            time.text = m.rappel?.let {
//                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
//            } ?: "--:--"
//
//            // Icône selon l’état
//            imgInfo.setImageResource(
//                if (m.isTaken) R.drawable.check
//                else R.drawable.attention
//            )
//
//            // Au clic : on inverse l’état et on rafraîchit cette vue
//            imgInfo.setOnClickListener {
//                m.isTaken = !m.isTaken
//                // soit on rebinde directement :
//                imgInfo.setImageResource(
//                    if (m.isTaken) R.drawable.check
//                    else R.drawable.attention
//                )
//                // ou bien on notifie l’item pour redessiner tout
//                // notifyItemChanged(adapterPosition)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
//        LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_medicament, parent, false)
//    )
//
//    override fun getItemCount() = items.size
//
//    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
//        holder.bind(items[pos])
//    }
//
//    fun add(m: Medicament) {
//        items.add(m)
//        notifyItemInserted(items.size - 1)
//    }
//}