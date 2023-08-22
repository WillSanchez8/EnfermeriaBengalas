package com.example.enfermeriabengalas.adapters

import android.content.res.Resources
import android.graphics.Color
import android.text.TextUtils
import com.example.enfermeriabengalas.databinding.MedicineItemBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.models.Medicine
//import com.example.enfermeriabengalas.viewmodel.ButtonState
import com.example.enfermeriabengalas.viewmodel.MedicineViewModel
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

// En com.example.enfermeriabengalas.adapters.MedicineAdapter.kt
interface MedicineAdapterListener {
    fun onEditButtonClicked(medicine: Medicine)
    fun onDeleteButtonClicked(medicine: Medicine)
    fun onPlusQuantityButtonClicked(medicine: Medicine)
    fun onMinusQuantityButtonClicked(medicine: Medicine)
}

class MedicineAdapter(
    var medicines: List<Medicine>,
    val listener: MedicineAdapterListener,
    val viewModel: MedicineViewModel
) : RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {

    //var buttonState: ButtonState? = null
    /*init {
        viewModel.buttonState.observeForever { state ->
            buttonState = state
            notifyDataSetChanged()
        }
    }*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = MedicineItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicines[position]
        when {
            medicine.quantity >= 5 -> holder.viewBinding.cardView.setCardBackgroundColor(Color.parseColor("#98FB98")) // Verde pistache
            medicine.quantity in 1..4 -> holder.viewBinding.cardView.setCardBackgroundColor(Color.parseColor("#F0E68C")) // Amarillo paja suave
            medicine.quantity == 0 -> holder.viewBinding.cardView.setCardBackgroundColor(Color.parseColor("#FFB6C1")) // Rojo suave
        }

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
        val descriptionTextView = holder.viewBinding.medicineDescription
        descriptionTextView.maxLines = 1
        descriptionTextView.ellipsize = TextUtils.TruncateAt.END

        cardView.setOnClickListener {
            if (descriptionTextView.maxLines == 1) {
                descriptionTextView.maxLines = Int.MAX_VALUE
            } else {
                descriptionTextView.maxLines = 1
            }
        }

        cardView.translationX = screenWidth.toFloat() // Comenzar fuera de la pantalla a la derecha
        cardView.animate()
            .translationX(0f) // Animar a su posición final
            .setDuration(300) // Establecer la duración de la animación
            .start() // Iniciar la animación

       /*val state = buttonState
        if (state != null) {
            holder.viewBinding.editButton.isEnabled = state.isEditMedicineEnabled
            holder.viewBinding.deleteButton.isEnabled = state.isDeleteMedicineEnabled
            holder.viewBinding.plusButton.isEnabled = state.isPlusQuantityButtonEnabled
            holder.viewBinding.minusButton.isEnabled = state.isMinusQuantityButtonEnabled
        }
        */
        // Establecer los listeners para los botones
        holder.viewBinding.editButton.setOnClickListener {
            viewModel.medicineToEdit.value = medicine
            listener.onEditButtonClicked(medicine)
        }
        holder.viewBinding.deleteButton.setOnClickListener {
            listener.onDeleteButtonClicked(medicine)
        }
        holder.viewBinding.plusButton.setOnClickListener {
            listener.onPlusQuantityButtonClicked(medicine)
        }
        holder.viewBinding.minusButton.setOnClickListener {
            listener.onMinusQuantityButtonClicked(medicine)
        }
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