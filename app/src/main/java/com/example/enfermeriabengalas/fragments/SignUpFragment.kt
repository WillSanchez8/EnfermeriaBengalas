package com.example.enfermeriabengalas.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpFragment : Fragment() {

    private lateinit var auth:FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener una instancia de ConnectivityManager
        connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Crear un NetworkCallback para escuchar cambios en la conectividad de red
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                // Mostrar un Snackbar con un mensaje y un emoji triste
                showErrorSnackbar("Estás en modo offline 😔, intenta conectarte a internet")
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Mostrar un Snackbar con un mensaje y un emoji feliz
                showSuccessSnackbar("Se ha restablecido la conexión 😊")
            }
        }

        // Registrar el NetworkCallback para recibir notificaciones de cambios en la conectividad de red
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Anular el registro del NetworkCallback cuando el fragmento se destruye
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()

        // Agregar TextWatchers a los campos de texto
        binding.etNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateField(s.toString(), binding.tilNameInput)
            }
        })

        binding.etEmailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateField(s.toString(), binding.tilEmailInput)
            }
        })

        binding.etPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateField(s.toString(), binding.tilPasswordInput)
            }
        })

        binding.etRepeatPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateField(s.toString(), binding.tilRepeatPasswordInput)
            }
        })
    }

    private fun init(view: View){
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }

    private fun showSuccessSnackbar(message: String) {
        val contextView = view
        if (contextView != null) {
            val snackbarText = SpannableStringBuilder(message)
            snackbarText.setSpan(ForegroundColorSpan(Color.WHITE), 0, snackbarText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            snackbarText.setSpan(StyleSpan(Typeface.BOLD), 0, snackbarText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Crear un color verde más suave
            val backgroundColor = Color.rgb(0, 200, 0)

            Snackbar.make(contextView, snackbarText, Snackbar.LENGTH_SHORT).setBackgroundTint(backgroundColor).show()
        }
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
    private fun registerEvents() {
        binding.authTextView.setOnClickListener {
            navControl.navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etNameInput.text.toString()
            val email = binding.etEmailInput.text.toString().trim()
            val pass = binding.etPasswordInput.text.toString().trim()
            val verifyPass = binding.etRepeatPasswordInput.text.toString().trim() //trim() elimina espacios en blanco
            val cargo = binding.spinnerCargo.selectedItem.toString()

            // Validar si los campos están vacíos
            var isValid = true
            isValid = validateField(name, binding.tilNameInput) && isValid
            isValid = validateField(email, binding.tilEmailInput) && isValid
            isValid = validateField(pass, binding.tilPasswordInput) && isValid
            isValid = validateField(verifyPass, binding.tilRepeatPasswordInput) && isValid

            // Validar si la contraseña tiene al menos 8 caracteres
            if (isValid) {
                // Validar si la contraseña tiene al menos 8 caracteres
                if (pass.length < 8) {
                    showErrorSnackbar("La contraseña debe tener al menos 8 caracteres")
                    isValid = false
                }

                // Validar si las contraseñas coinciden
                if (pass != verifyPass) {
                    showErrorSnackbar("Las contraseñas no coinciden")
                    isValid = false
                }
            }

            if (isValid) {
                binding.progressBar2.visibility = View.VISIBLE
                view?.let { contextView ->
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // El usuario se registró correctamente
                            val user = auth.currentUser
                            user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    // El correo electrónico de verificación se envió correctamente
                                    // Mostrar un mensaje al usuario indicando que debe verificar su dirección de correo electrónico
                                    Snackbar.make(contextView, "Se ha enviado un correo electrónico de verificación a tu dirección de correo. Por favor, verifica tu dirección para poder iniciar sesión.", Snackbar.LENGTH_LONG).show()
                                    navControl.navigate(R.id.action_signUpFragment_to_signInFragment)
                                } else {
                                    // Ocurrió un error al enviar el correo electrónico de verificación
                                    Snackbar.make(contextView, emailTask.exception?.message ?: "Ocurrió un error al enviar el correo electrónico de verificación", Snackbar.LENGTH_SHORT).show()
                                }
                                binding.progressBar2.visibility = View.GONE
                            }

                            //Guardar el nombre y el cargo del usuario en la base de datos
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                databaseRef =
                                    FirebaseDatabase.getInstance().reference.child("users").child(uid)
                                databaseRef.child("name").setValue(name).addOnCompleteListener { nameTask ->
                                    if (!nameTask.isSuccessful) {
                                        showErrorSnackbar("Error al guardar el nombre del usuario en la base de datos")
                                    }
                                    binding.progressBar2.visibility = View.GONE
                                }
                                databaseRef.child("cargo").setValue(cargo).addOnCompleteListener { cargoTask ->
                                    if (!cargoTask.isSuccessful) {
                                        showErrorSnackbar("Error al guardar el cargo del usuario en la base de datos")
                                    }
                                    binding.progressBar2.visibility = View.GONE
                                }
                            }
                        } else {
                            when (task.exception) {
                                is FirebaseAuthUserCollisionException -> {
                                    // El correo electrónico ya está asociado a una cuenta existente
                                    showErrorSnackbar("Este correo ya está asociado, inicie sesión o inténtelo nuevamente")
                                }
                                else -> {
                                    Snackbar.make(
                                        contextView,
                                        task.exception?.message ?: "Ocurrió un error",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            binding.progressBar2.visibility = View.GONE
                        }
                    }
                } ?: run {
                    // La vista es nula, el fragmento ya no está asociado a una actividad, no se puede mostrar un Snackbar
                    // Mostrar un mensaje en el registro de la aplicación
                    showErrorSnackbar("No se puede mostrar el Snackbar porque la vista es nula")
                }
            } else {
                showErrorSnackbar("Por favor, complete todos los campos")
            }
        }
    }
}