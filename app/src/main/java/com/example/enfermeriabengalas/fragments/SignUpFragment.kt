package com.example.enfermeriabengalas.fragments

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
    // Definir un CHANNEL_ID para el canal de notificación
    val CHANNEL_ID = "account_deleted_channel"
    val NOTIFICATION_ID = 1

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

        // Crear un canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Registrar el canal de notificación en el sistema
            val notificationManager: NotificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

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

    fun scheduleAccountDeletion() {
        // Crear un Handler y un Runnable para ejecutar una función después de 24 horas
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            // Verificar si el usuario ha verificado su dirección de correo electrónico
            val user = auth.currentUser
            user?.reload()?.addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful) {
                    if (!user.isEmailVerified) {
                        // El usuario no ha verificado su dirección de correo electrónico
                        // Borrar la cuenta del usuario y sus datos asociados
                        val uid = user.uid
                        val databaseRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
                        databaseRef.removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Los datos del usuario se eliminaron correctamente
                                user.delete().addOnCompleteListener { deleteTask ->
                                    if (deleteTask.isSuccessful) {
                                        // La cuenta del usuario se eliminó correctamente
                                        // Enviar una notificación al usuario utilizando Firebase Cloud Messaging
                                        sendAccountDeletedNotification()
                                    } else {
                                        showErrorSnackbar("Error al eliminar la cuenta del usuario")
                                    }
                                }
                            } else {
                                showErrorSnackbar("Error al eliminar los datos del usuario")
                            }
                        }
                    }
                } else {
                    showErrorSnackbar("Error al verificar el estado de la cuenta del usuario")
                }
            }
        }
        // Programar la ejecución del Runnable después de 24 horas (en milisegundos)
        handler.postDelayed(runnable, 24 * 60 * 60 * 1000)
    }

    fun sendAccountDeletedNotification() {
        // Obtener una referencia al contexto de la actividad
        val context = requireContext()

        // Obtener el nombre del paquete de la aplicación
        val packageName = context.packageName

        // Crear una notificación para mostrar al usuario
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_tiger_sad)
            .setContentTitle("Cuenta eliminada")
            .setContentText("Lo sentimos 😔, tu cuenta ha sido eliminada porque no verificaste tu correo electrónico a tiempo. Por favor, inténtalo de nuevo.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(Uri.parse("android.resource://$packageName/raw/sound_tiger")) // Reproducir un sonido personalizado

        // Verificar si el usuario ha otorgado el permiso para hacer vibrar el dispositivo
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            // El usuario ha otorgado el permiso
            // Hacer vibrar el dispositivo
            builder.setVibrate(longArrayOf(0, 1000, 500, 1000))
        }

        // Mostrar la notificación
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
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
            if (!isValid) {
                showErrorSnackbar("Por favor, complete todos los campos")
            } else {
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
                                    Snackbar.make(contextView, "Si no verificas tu correo electrónico en las próximas 24 horas, tu cuenta será borrada.", Snackbar.LENGTH_LONG).show()
                                    // Llamar a la función scheduleAccountDeletion
                                    scheduleAccountDeletion()
                                    navControl.navigate(R.id.action_signUpFragment_to_signInFragment)

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
                                    // Ocurrió un error al enviar el correo electrónico de verificación
                                    Snackbar.make(contextView, emailTask.exception?.message ?: "Ocurrió un error al enviar el correo electrónico de verificación", Snackbar.LENGTH_SHORT).show()
                                }
                                binding.progressBar2.visibility = View.GONE
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
                                        task.exception?.message ?: "Ocurrió un error al crear la cuenta",
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
            }
        }
    }
}