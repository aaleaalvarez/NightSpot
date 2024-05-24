package com.example.proyecto

import DiscoViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DiscoAdapter(private var discos: MutableList<Disco> = mutableListOf()) :
    RecyclerView.Adapter<DiscoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoViewHolder {
        // Infla el layout usando LayoutInflater y luego pasa la vista inflada al constructor de DiscoViewHolder.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_disco, parent, false)
        return DiscoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiscoViewHolder, position: Int) {
        // Utiliza el método bind del DiscoViewHolder para asignar los datos del objeto Disco a los views.
        holder.bind(discos[position])
    }

    override fun getItemCount(): Int {
        // Retorna el tamaño de la lista de discos.
        return discos.size
    }

    // Método para actualizar la lista completa de discos y notificar al adaptador del cambio.
    fun updateDiscos(newDiscos: List<Disco>) {
        discos.clear()
        discos.addAll(newDiscos)
        notifyDataSetChanged()
    }

    // Método para agregar un solo disco a la lista y notificar al adaptador de la inserción.
    fun addDisco(disco: Disco) {
        discos.add(disco)
        notifyItemInserted(discos.size - 1)
    }

    // Método para limpiar todos los discos de la lista y notificar al adaptador del cambio.
    fun clear() {
        discos.clear()
        notifyDataSetChanged()
    }
}

