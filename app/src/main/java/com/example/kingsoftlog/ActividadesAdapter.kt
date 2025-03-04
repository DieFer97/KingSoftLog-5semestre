package com.example.kingsoftlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActividadesAdapter(private val actividades: List<String>) :
    RecyclerView.Adapter<ActividadesAdapter.ActividadViewHolder>() {

    class ActividadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textActividad: TextView = view.findViewById(R.id.text_actividad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actividad, parent, false)
        return ActividadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        holder.textActividad.text = actividades[position]
    }

    override fun getItemCount(): Int = actividades.size
}
