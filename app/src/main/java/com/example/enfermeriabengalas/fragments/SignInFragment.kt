package com.example.enfermeriabengalas.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.FragmentSignInBinding
import com.example.enfermeriabengalas.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class SignInFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
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
    }

    private fun registerEvents() {
        binding.authTextView.setOnClickListener {
            navControl.navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmailInput.text.toString()
            val pass = binding.etPasswordInput.text.toString()

            // Validar si los campos están vacíos
            var isValid = true
            isValid = validateField(email, binding.tilEmailInput) && isValid
            isValid = validateField(pass, binding.tilPasswordInput) && isValid

            if (isValid) {
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    view?.let { contextView ->
                        if (task.isSuccessful) {
                            Snackbar.make(contextView, "Sesión iniciada correctamente", Snackbar.LENGTH_SHORT).show()
                            navControl.navigate(R.id.action_signInFragment_to_homeFragment)
                        } else {
                            if (task.exception is FirebaseAuthInvalidCredentialsException) {
                                // El correo electrónico o la contraseña son incorrectos
                                val snackbarText = SpannableStringBuilder("El correo o la contraseña son inválidos, inténtalo nuevamente")
                                snackbarText.setSpan(ForegroundColorSpan(Color.WHITE), 0, snackbarText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                snackbarText.setSpan(StyleSpan(Typeface.BOLD), 0, snackbarText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                Snackbar.make(contextView, snackbarText, Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).show()
                            } else {
                                Snackbar.make(contextView, task.exception?.message ?: "", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    } ?: run {
                        // La vista es nula, el fragmento ya no está asociado a una actividad, no se puede mostrar un Snackbar
                        // Mostrar un mensaje en el registro de la aplicación
                        Log.w("SignInFragment", "No se puede mostrar el Snackbar porque la vista es nula")
                    }
                }
            }
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
}