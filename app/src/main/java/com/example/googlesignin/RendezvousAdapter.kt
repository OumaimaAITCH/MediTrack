package com.example.googlesignin

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class RendezvousAdapter(
    private var rendezvousList: List<Rendezvous>,
    private val onDeleteClick: (Rendezvous) -> Unit
) : RecyclerView.Adapter<RendezvousAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvObjet: TextView = view.findViewById(R.id.tvObjet)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val ivStatus: ImageView = view.findViewById(R.id.ivStatus)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
        var isChecked = false

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rendezvous, parent, false)
        return ViewHolder(view)

    }

    @SuppressLint("ServiceCast")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val rendezvous = rendezvousList[position]
        holder.tvObjet.text = rendezvous.objet
        holder.tvDateTime.text = rendezvous.dateHeure

        holder.ivStatus.setOnClickListener {
            holder.isChecked = !holder.isChecked
            holder.ivStatus.setImageResource(
                if (holder.isChecked) R.drawable.checkgreen
                else R.drawable.attention
            )

            if (holder.isChecked) {
                // Arrêter la sonnerie
                AppointmentAlarmReceiver.stopRingtone()

                // Annuler la notification
                val notificationManager = holder.itemView.context
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(rendezvous.id.hashCode())
            }
        }
        holder.ivDelete.setOnClickListener {
            onDeleteClick(rendezvous)
        }
    }

    override fun getItemCount(): Int = rendezvousList.size

    fun updateData(newList: List<Rendezvous>) {
        rendezvousList = newList
        notifyDataSetChanged()
    }

}