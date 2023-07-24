package com.example.enfermeriabengalas.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.FragmentAddMedicineBinding
import com.example.enfermeriabengalas.models.Medicine
import com.example.enfermeriabengalas.viewmodel.MedicineViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class AddMedicineFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentAddMedicineBinding
    private lateinit var databaseRef: DatabaseReference
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    private var selectedImageUri: Uri? = null
    private lateinit var viewModel: MedicineViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentAddMedicineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()

        // Observa cambios en la propiedad medicineToEdit del ViewModel
        viewModel.medicineToEdit.observe(viewLifecycleOwner) { medicine ->
            if (medicine != null) {
                // El usuario está editando un medicamento existente
                // Cambia el título del fragmento a "Editar Medicamento"
                binding.tvSignupTitle.text = getString(R.string.title_edit_medicine)
                // Cambia el texto del botón a "Guardar cambios"
                binding.btnAddMedicine.text = getString(R.string.save_changes)

                // Completa los campos de texto con los valores actuales del medicamento
                binding.etMedicineNameInput.setText(medicine.name)
                binding.etMedicineDescriptionInput.setText(medicine.description)
                binding.etMedicineQuantityInput.setText(medicine.quantity.toString())
                val categoryIndex = resources.getStringArray(R.array.category_options).indexOf(medicine.category)
                binding.spinnerMedicineCategory.setSelection(categoryIndex)
                val imageUrl = medicine.image
                if (imageUrl != null) {
                    Picasso.get()
                        .load(imageUrl)
                        .into(binding.ivLogo)
                } else {
                    // Mostrar una imagen por defecto
                    val defaultImage = ContextCompat.getDrawable(requireContext(), R.drawable.icon_medicine)
                    binding.ivLogo.setImageDrawable(defaultImage)
                }
            } else {
                // El usuario está agregando un nuevo medicamento
                // Cambia el título del fragmento a "Nuevo Medicamento"
                binding.tvSignupTitle.text = getString(R.string.title_new_medicine)
                // Cambia el texto del botón a "Agregar medicamento"
                binding.btnAddMedicine.text = getString(R.string.add_medicine)
            }
        }

        // Habilita el contador de caracteres para los campos de nombre y descripción del medicamento
        binding.tilMedicineNameInput.isCounterEnabled = true
        binding.tilMedicineNameInput.counterMaxLength = 25
        binding.tilMedicineDescriptionInput.isCounterEnabled = true
        binding.tilMedicineDescriptionInput.counterMaxLength = 100

        // Agregar TextWatchers a los campos de texto
        binding.etMedicineNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.contains("\n")) {
                    val newText = s.toString().replace("\n", "")
                    binding.etMedicineNameInput.setText(newText)
                    binding.etMedicineNameInput.setSelection(newText.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Habilitar el desplazamiento vertical en el campo de texto
        binding.etMedicineDescriptionInput.movementMethod = ScrollingMovementMethod()

        // Agregar TextWatcher al campo de texto
        binding.etMedicineDescriptionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etMedicineQuantityInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "0") {
                    binding.tilMedicineQuantityInput.error = "La cantidad debe ser mayor a 0"
                    binding.btnAddMedicine.isEnabled = false
                } else if (s.toString().isNotEmpty() && s.toString().toInt() > 999) {
                    binding.tilMedicineQuantityInput.error = "La cantidad debe ser menor a 1000"
                    binding.btnAddMedicine.isEnabled = false
                } else {
                    binding.tilMedicineQuantityInput.error = null
                    binding.btnAddMedicine.isEnabled = true
                }
            }
        })
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navControl = Navigation.findNavController(view)
        databaseRef = FirebaseDatabase.getInstance().reference
        viewModel = ViewModelProvider(requireActivity()).get(MedicineViewModel::class.java)
        viewModel.init(databaseRef)
    }

    override fun onPause() {
        super.onPause()
        viewModel.medicineToEdit.value = null
    }

    private fun showErrorSnackbar(message: String) {
        val contextView = view
        if (contextView != null) {
            val snackbarText = SpannableStringBuilder(message)
            snackbarText.setSpan(ForegroundColorSpan(Color.WHITE), 0, snackbarText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            snackbarText.setSpan(StyleSpan(Typeface.BOLD), 0, snackbarText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            Snackbar.make(contextView, snackbarText, Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).show()
        }
    }

    fun validateField(field: String, inputLayout: TextInputLayout): Boolean {
        return if (field.isEmpty()) {
            inputLayout.setError("Este campo no debe estar vacío")
            false
        } else {
            inputLayout.setError(null)
            true
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Este código se ejecutará cuando se reciba el resultado de la actividad
        if (uri != null) {
            // El usuario seleccionó una imagen
            // Actualiza el icono en la interfaz de usuario
            binding.ivLogo.setImageURI(uri)
            selectedImageUri = uri
        }
    }

    private fun registerEvents() {

        binding.btnChangeIcon.setOnClickListener {
            // Verifica si la aplicación tiene permiso para leer el almacenamiento externo
            binding.btnChangeIcon.setOnClickListener {
                // Verifica si la aplicación tiene permiso para leer el almacenamiento externo
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // El permiso para leer el almacenamiento externo ya está concedido
                    // Abre el selector de imágenes
                    pickImageLauncher.launch("image/*")
                } else {
                    // Solicita el permiso para leer el almacenamiento externo
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE)
                }
            }
        }

        binding.btnAddMedicine.setOnClickListener {
            val medicineName = binding.etMedicineNameInput.text.toString()
            val medicineDescription = binding.etMedicineDescriptionInput.text.toString()
            val medicineQuantity = binding.etMedicineQuantityInput.text.toString().toIntOrNull() ?: 0
            val medicineCategory = binding.spinnerMedicineCategory.selectedItem.toString()

            var isValid = true
            isValid = validateField(medicineName, binding.tilMedicineNameInput) && isValid
            isValid = validateField(medicineDescription, binding.tilMedicineDescriptionInput) && isValid
            isValid = validateField(medicineQuantity.toString(), binding.tilMedicineQuantityInput) && isValid

            if (isValid) {
                binding.progressBar2.visibility = View.VISIBLE
                // Crea una instancia de Medicine con los datos ingresados por el usuario
                val medicine = Medicine(
                    name = medicineName,
                    description = medicineDescription,
                    quantity = medicineQuantity,
                    category = medicineCategory,
                    image = if (viewModel.medicineToEdit.value != null && selectedImageUri == null) viewModel.medicineToEdit.value?.image else selectedImageUri?.toString()
                )

                if (viewModel.medicineToEdit.value == null) {
                    // El usuario está agregando un nuevo medicamento
                    // Llama a la función addMedicine del ViewModel para agregar el medicamento a la base de datos
                    viewModel.addMedicine(medicine, onSuccess = {
                        binding.progressBar2.visibility = View.GONE
                        navControl.navigate(R.id.action_addMedicineFragment_to_homeFragment)
                        Snackbar.make(binding.root, "Medicamento agregado con éxito", Snackbar.LENGTH_SHORT).show()
                    }, onFailure = { message ->
                        binding.progressBar2.visibility = View.GONE
                        showErrorSnackbar(message)
                    })
                } else {
                    // El usuario está editando un medicamento existente
                    // Llama a la función updateMedicine del ViewModel para actualizar el medicamento en la base de datos
                    // El usuario está editando un medicamento existente
                    // Carga la imagen del medicamento en la vista
                    viewModel.updateMedicine(viewModel.medicineToEdit.value!!, medicine, onSuccess = {
                        binding.progressBar2.visibility = View.GONE
                        navControl.navigate(R.id.action_addMedicineFragment_to_medicineFragment)
                        Snackbar.make(binding.root, "Cambios guardados con éxito", Snackbar.LENGTH_SHORT).show()
                    }, onFailure = { message ->
                        binding.progressBar2.visibility = View.GONE
                        showErrorSnackbar(message)
                    })
                    // Establece la variable medicineToEdit a null
                    viewModel.medicineToEdit.value = null
                }
            } else {
                showErrorSnackbar("Por favor, complete todos los campos")
            }
        }
    }
}