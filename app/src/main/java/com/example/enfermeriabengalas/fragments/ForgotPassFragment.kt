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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.FragmentForgotPassBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ForgotPassFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentForgotPassBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentForgotPassBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()
        binding.etEmailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateField(s.toString(), binding.tilEmailInput)
            }
        })
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navControl = Navigation.findNavController(view)
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

    fun registerEvents() {
        binding.btnSendEmail.setOnClickListener {
            if (validateField(binding.etEmailInput.text.toString(), binding.tilEmailInput)) {
                // Obtener el valor del campo de correo electrónico
                val email = binding.etEmailInput.text.toString()
                binding.progressBar2.visibility = View.VISIBLE
                // Enviar la solicitud de restablecimiento de contraseña
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    view?.let { contextView ->
                        if (task.isSuccessful) {
                            // Mostrar un mensaje al usuario indicando que se ha enviado el correo electrónico
                            Snackbar.make(
                                contextView,
                                "Le hemos enviado un correo electrónico. Revise su bandeja de entrada.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            // Navegar al fragmento de inicio de sesión
                            navControl.navigate(R.id.action_forgotPassFragment_to_signInFragment)
                        } else {
                            // Manejar errores al enviar el correo electrónico
                            showErrorSnackbar("No se pudo enviar el correo electrónico")
                        }
                    }
                    binding.progressBar2.visibility = View.GONE
                }
            }
        }
    }
}