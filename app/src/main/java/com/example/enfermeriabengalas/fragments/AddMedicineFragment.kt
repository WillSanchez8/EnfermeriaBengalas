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
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.FragmentAddMedicineBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class AddMedicineFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentAddMedicineBinding
    private lateinit var databaseRef: DatabaseReference
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    private var selectedImageUri: Uri? = null

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

        // Agregar TextWatchers a los campos de texto
        binding.etMedicineNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.etMedicineDescriptionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.etMedicineQuantityInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navControl = Navigation.findNavController(view)
        databaseRef = FirebaseDatabase.getInstance().reference
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
        binding.backButton.setOnClickListener {
            navControl.navigate(R.id.action_addMedicineFragment_to_homeFragment)
        }

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
            val medicineQuantity = binding.etMedicineQuantityInput.text.toString()
            val medicineCategory = binding.spinnerMedicineCategory.selectedItem.toString()

            var isValid = true
            isValid = validateField(medicineName, binding.tilMedicineNameInput) && isValid
            isValid = validateField(medicineDescription, binding.tilMedicineDescriptionInput) && isValid
            isValid = validateField(medicineQuantity, binding.tilMedicineQuantityInput) && isValid

            if (isValid) {
                binding.progressBar2.visibility = View.VISIBLE
                // Asigna el valor de selectedImageUri a una variable local
                val imageUri = selectedImageUri

                // Comprueba si imageUri no es nula
                if (imageUri != null) {
                    // Genera un nombre de archivo único utilizando un UUID
                    val fileName = "imagen_${UUID.randomUUID()}.jpg"
                    // Crea una referencia a la ruta completa del archivo en Firebase Storage
                    val storageRef = Firebase.storage.reference.child("imagenes/$fileName")

                    // Sube el archivo a Firebase Storage
                    storageRef.putFile(imageUri)
                        .addOnSuccessListener {
                            // El archivo se subió correctamente
                            // Obtiene la URL de descarga del archivo
                            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                // Guarda los datos del medicamento en la base de datos, incluyendo la URL de descarga de la imagen
                                val medicine = hashMapOf(
                                    "name" to medicineName,
                                    "description" to medicineDescription,
                                    "quantity" to medicineQuantity,
                                    "category" to medicineCategory,
                                    "image" to downloadUrl.toString()
                                )

                                // Guarda los datos del medicamento en una ruta global accesible para todos los usuarios
                                databaseRef.child("medicines").push().setValue(medicine)
                                    .addOnSuccessListener {
                                        navControl.navigate(R.id.action_addMedicineFragment_to_homeFragment)
                                        Snackbar.make(binding.root, "Medicamento agregado con éxito", Snackbar.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        showErrorSnackbar("Error al agregar el medicamento")
                                    }
                            }
                        }
                        .addOnFailureListener {
                            showErrorSnackbar("Error al subir la imagen")
                        }
                } else {
                    // imageUri es nula
                    // Guarda los datos del medicamento en la base de datos sin incluir la URL de descarga de la imagen
                    val medicine = hashMapOf(
                        "name" to medicineName,
                        "description" to medicineDescription,
                        "quantity" to medicineQuantity,
                        "category" to medicineCategory
                    )

                    // Guarda los datos del medicamento en una ruta global accesible para todos los usuarios
                    databaseRef.child("medicines").push().setValue(medicine)
                        .addOnSuccessListener {
                            navControl.navigate(R.id.action_addMedicineFragment_to_homeFragment)
                            Snackbar.make(binding.root, "Medicamento agregado con éxito", Snackbar.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            showErrorSnackbar("Error al agregar el medicamento")
                        }
                }
                binding.progressBar2.visibility = View.GONE
            } else {
                showErrorSnackbar("Por favor, complete todos los campos")
            }
        }
    }
}