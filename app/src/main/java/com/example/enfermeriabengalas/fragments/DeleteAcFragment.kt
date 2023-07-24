package com.example.enfermeriabengalas.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.DialogReauthenticateBinding
import com.example.enfermeriabengalas.databinding.FragmentDeleteAcBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DeleteAcFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentDeleteAcBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding  = FragmentDeleteAcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()
    }

    private fun init(view: View) {
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
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

    private fun registerEvents() {
        binding.btnCancelar.setOnClickListener {
            navControl.navigate(R.id.action_deleteAcFragment_to_homeFragment)
        }

        binding.btnSi.setOnClickListener {
            // Mostrar un cuadro de diálogo para solicitar el correo electrónico y la contraseña del usuario
            showReauthenticateDialog()
        }
    }

    private fun showReauthenticateDialog() {
        // Inflar el diseño del cuadro de diálogo utilizando View Binding
        val dialogBinding = DialogReauthenticateBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reautenticar")
            .setMessage("Por favor, ingresa tu correo electrónico y contraseña para continuar.")
            .setView(dialogBinding.root) // Establecer la vista del cuadro de diálogo utilizando la raíz del binding
            .setNegativeButton("Cancelar") { dialog, which ->
                // No hacer nada si el usuario cancela la reautenticación
                dialog.dismiss()
            }
            .setPositiveButton("Aceptar") { dialog, which ->
                // Obtener el correo electrónico y la contraseña ingresados por el usuario utilizando las propiedades del binding
                val email = dialogBinding.email.text.toString().trim()
                val password = dialogBinding.password.text.toString().trim()
                // Verificar si los campos de correo electrónico y contraseña están vacíos o contienen solo espacios o saltos de línea
                if (email.isEmpty() || password.isEmpty()) {
                    // Mostrar un mensaje de error si alguno de los campos está vacío o contiene solo espacios o saltos de línea
                    showErrorSnackbar("Por favor, completa todos los campos")
                } else {
                    // Reautenticar al usuario utilizando el correo electrónico y la contraseña ingresados
                    reauthenticateUser(email, password)
                }
            }
            .show()
    }

    private fun reauthenticateUser(email: String, password: String) {
        // Crear una credencial utilizando el correo electrónico y la contraseña ingresados por el usuario
        val credential = EmailAuthProvider.getCredential(email, password)
        // Reautenticar al usuario utilizando la credencial creada
        auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // El usuario se reautenticó correctamente
                // Eliminar los datos del usuario en la base de datos
                deleteUserDatabaseData()
                // Eliminar la cuenta del usuario en Firebase Authentication
                deleteUserAuthAccount()
            } else {
                // Hubo un error al reautenticar al usuario
                val errorMessage = task.exception?.message ?: "Error desconocido"
                showErrorSnackbar("Error al reautenticar: $errorMessage")
            }
        }
    }

    private fun deleteUserDatabaseData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val databaseRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
            databaseRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // The user's data was deleted successfully
                    view?.let { Snackbar.make(it, "Lamentamos que te vayas 😔", Snackbar.LENGTH_SHORT).show() }
                } else {
                    // There was an error deleting the user's data
                    showErrorSnackbar("Ha sucedido un error al intentar eliminar tu cuenta, prueba de nuevo")
                }
            }
            // Eliminar el token de registro de FCM del usuario de la base de datos
            val tokenRef = FirebaseDatabase.getInstance().reference.child("tokens").child(uid)
            tokenRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    view?.let { Snackbar.make(it, "Tu dispositivo se ha eliminado de la base de datos", Snackbar.LENGTH_SHORT).show() }
                } else {
                    showErrorSnackbar("Ha sucedido un error al intentar eliminar tu dispositivo, prueba de nuevo")
                }
            }
        }
    }

    private fun deleteUserAuthAccount() {
        auth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // La cuenta del usuario se eliminó correctamente
                view?.let { Snackbar.make(it, "Cuenta eliminada con éxito", Snackbar.LENGTH_SHORT).show() }
            } else {
                // Hubo un error al eliminar la cuenta del usuario
                showErrorSnackbar("Tenemos problemas al eliminar tu cuenta, intenta de nuevo más tarde")
            }
        }
        navControl.navigate(R.id.action_deleteAcFragment_to_signInFragment)
    }
}

