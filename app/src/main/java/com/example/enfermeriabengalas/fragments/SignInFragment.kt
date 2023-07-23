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
import com.example.enfermeriabengalas.databinding.FragmentSignInBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignInFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSignInBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false)
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
    }

    private fun init(view: View) {
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("users")
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
            navControl.navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.forgotpassword.setOnClickListener {
            navControl.navigate(R.id.action_signInFragment_to_forgotPassFragment4)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailInput.text.toString().trim()
            val pass = binding.etPasswordInput.text.toString().trim()
            val cargo = binding.spinnerCargo.selectedItem.toString()

            // Validar si los campos están vacíos
            var isValid = true
            isValid = validateField(email, binding.tilEmailInput) && isValid
            isValid = validateField(pass, binding.tilPasswordInput) && isValid

            if (isValid) {
                binding.progressBar2.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    view?.let { contextView ->
                        if (task.isSuccessful) {
                            // El inicio de sesión fue exitoso
                            val user = auth.currentUser
                            user?.reload()?.addOnCompleteListener { reloadTask ->
                                if (reloadTask.isSuccessful) {
                                    if (user.isEmailVerified) {
                                        // La dirección de correo electrónico del usuario ha sido verificada
                                        // Obtener una referencia al nodo del usuario en la base de datos
                                        val uid = auth.currentUser?.uid.toString()
                                        val userRef = databaseRef.child(uid)

                                        // Leer el valor del campo "cargo" del usuario en la base de datos
                                        userRef.child("cargo").addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                val cargoInDatabase =
                                                    dataSnapshot.getValue(String::class.java)
                                                // Comprobar si el cargo es el mismo que se ingresó en la vista de inicio de sesión
                                                if (cargoInDatabase == cargo) {
                                                    // El cargo es correcto, continuar con el inicio de sesión
                                                    Snackbar.make(
                                                        contextView,
                                                        "Sesión iniciada correctamente",
                                                        Snackbar.LENGTH_SHORT
                                                    ).show()
                                                    navControl.navigate(R.id.action_signInFragment_to_homeFragment)
                                                } else {
                                                    // El cargo no es correcto, mostrar un mensaje de error
                                                    showErrorSnackbar("El cargo seleccionado no es el correspondiente, intente nuevamente")
                                                }
                                                binding.progressBar2.visibility = View.GONE
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                                // Mostrar un mensaje de error al usuario
                                                showErrorSnackbar("Ocurrió un error al intentar leer los datos. Inténtalo nuevamente.")
                                                binding.progressBar2.visibility = View.GONE
                                            }
                                        })
                                    } else {
                                        // La dirección de correo electrónico del usuario no ha sido verificada
                                        // Mostrar un mensaje al usuario indicando que debe verificar su dirección de correo electrónico
                                        showErrorSnackbar("Por favor, verifica tu dirección de correo electrónico para poder iniciar sesión.")
                                        binding.progressBar2.visibility = View.GONE
                                    }
                                } else {
                                    // Ocurrió un error al recargar la información del usuario
                                    showErrorSnackbar(reloadTask.exception?.message ?: "Ocurrió un error al recargar la información del usuario")
                                    binding.progressBar2.visibility = View.GONE
                                }
                            }
                        } else {
                            when (task.exception) {
                                is FirebaseAuthInvalidUserException -> {
                                    // El correo electrónico no está registrado o ha sido deshabilitado
                                    showErrorSnackbar("Este correo no está registrado o ha sido deshabilitado")
                                }
                                is FirebaseAuthInvalidCredentialsException -> {
                                    // La contraseña es incorrecta
                                    showErrorSnackbar("El correo o la contraseña son incorrectos, inténtelo nuevamente")
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
                    } ?: run {
                        // La vista es nula, el fragmento ya no está asociado a una actividad, no se puede mostrar un Snackbar
                        // Mostrar un mensaje en el registro de la aplicación
                        showErrorSnackbar("No se puede mostrar el Snackbar porque la vista es nula")
                    }
                }
            } else {
                showErrorSnackbar("Por favor, complete todos los campos")
            }
        }
    }
}