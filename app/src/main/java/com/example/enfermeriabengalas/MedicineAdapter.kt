import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Resources
import com.example.enfermeriabengalas.databinding.MedicineItemBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.models.Medicine
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

class MedicineAdapter(var medicines: List<Medicine>) : RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = MedicineItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.medicineNameTextView.text = medicine.name
        holder.medicineDescriptionTextView.text = medicine.description
        holder.medicineQuantityTextView.text = medicine.quantity.toString()
        holder.medicineCategoryTextView.text = medicine.category
        val medicineImage = holder.viewBinding.medicineImage
        val imageUrl = medicine.image
        if (imageUrl != null) {
            Picasso.get()
                .load(imageUrl)
                .resize(256, 256)
                .into(medicineImage)
        } else {
            // Mostrar una imagen por defecto
            val defaultImage = ContextCompat.getDrawable(holder.itemView.context, R.drawable.icon_medicine)
            medicineImage.setImageDrawable(defaultImage)
        }

        // Obtener el ancho de la pantalla en píxeles
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // Agregar animación al CardView
        val cardView = holder.itemView as MaterialCardView
        cardView.translationX = screenWidth.toFloat() // Comenzar fuera de la pantalla a la derecha
        cardView.animate()
            .translationX(0f) // Animar a su posición final
            .setDuration(300) // Establecer la duración de la animación
            .start() // Iniciar la animación
    }

    override fun getItemCount(): Int = medicines.size

    inner class ViewHolder(val viewBinding: MedicineItemBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        val medicineNameTextView = viewBinding.medicineName
        val medicineDescriptionTextView = viewBinding.medicineDescription
        val medicineQuantityTextView = viewBinding.medicineAvailability
        val medicineCategoryTextView = viewBinding.medicineCategory
        val medicineImage = viewBinding.medicineImage
    }
}