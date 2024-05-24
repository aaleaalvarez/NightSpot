import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.proyecto.Disco
import com.example.proyecto.R
import com.example.proyecto.activities.InfoDiscoActivity

class DiscoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageViewClub: ImageView = itemView.findViewById(R.id.imageViewDisco)
    private val textViewClubName: TextView = itemView.findViewById(R.id.textViewDiscoName)
    private val textViewClubDescription: TextView =
        itemView.findViewById(R.id.textViewDiscoDescription)

    fun bind(disco: Disco) {
        textViewClubName.text = disco.name
        textViewClubDescription.text = disco.description

        if (disco.imageURL.isNotEmpty()) {
            Log.d("DiscoViewHolder", "Intentando cargar la URL de la imagen: ${disco.imageURL}")
            Glide.with(itemView.context)
                .load(disco.imageURL)
                .placeholder(R.drawable.loading_animation)  // Reemplaza con tu recurso de animación de carga
                .error(R.drawable.image_load_error)  // Reemplaza con tu recurso de imagen de error
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("GlideError", "Error al cargar la imagen", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("GlideSuccess", "Imagen cargada con éxito")
                        return false
                    }
                })
                .into(imageViewClub)
        } else {
            Log.d("DiscoViewHolder", "URL de imagen vacía, mostrando el logo predeterminado.")
            imageViewClub.setImageResource(R.drawable.logo)  // Reemplaza con tu recurso de logo predeterminado
        }

        itemView.setOnClickListener {
            val context = itemView.context
            val intent = Intent(context, InfoDiscoActivity::class.java).apply {
                putExtra("ID", disco.id)
                putExtra("NAME", disco.name)
                putExtra("DESCRIPTION", disco.description)
                putExtra("IMAGE_URL", disco.imageURL)
                putExtra("LATITUDE", disco.location.latitude)
                putExtra("LONGITUDE", disco.location.longitude)
            }
            context.startActivity(intent)
        }
    }
}
