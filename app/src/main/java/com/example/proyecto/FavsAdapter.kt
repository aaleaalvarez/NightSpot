package com.example.proyecto

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.proyecto.databinding.ItemLocalBinding

class FavsAdapter(private val locales: List<Local>) :
    RecyclerView.Adapter<FavsAdapter.LocalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalViewHolder {
        val binding = ItemLocalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocalViewHolder, position: Int) {
        val local = locales[position]
        holder.bind(local)
    }

    override fun getItemCount() = locales.size

    class LocalViewHolder(private val binding: ItemLocalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(local: Local) {
            binding.textViewLocalName.text = local.name

            if (local.imageUrl.isNotEmpty()) {
                Log.d("LocalViewHolder", "Intentando cargar la URL de la imagen: ${local.imageUrl}")
                Glide.with(binding.imageViewLocalPhoto.context)
                    .load(local.imageUrl)
                    .placeholder(R.drawable.loading_animation) // Muestra un GIF o imagen estática mientras carga
                    .error(R.drawable.image_load_error) // Muestra una imagen de error si la carga falla
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("GlideError", "Error al cargar la imagen: ${local.imageUrl}", e)
                            return false // false permite que Glide maneje el error
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("GlideSuccess", "Imagen cargada con éxito: ${local.imageUrl}")
                            return false
                        }
                    })
                    .into(binding.imageViewLocalPhoto)
            } else {
                Log.d("LocalViewHolder", "URL de imagen vacía, mostrando imagen predeterminada.")
                binding.imageViewLocalPhoto.setImageResource(R.drawable.logo) // Cambiar 'default_image' por tu imagen predeterminada
            }
        }
    }

}
