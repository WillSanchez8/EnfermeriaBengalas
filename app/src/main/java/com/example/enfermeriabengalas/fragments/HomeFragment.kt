package com.example.enfermeriabengalas.fragments

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.FragmentHomeBinding
import com.example.enfermeriabengalas.viewmodel.MedicineViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentHomeBinding
    private var phoneNumber = "5514185533"
    private val REQUEST_CALL_PHONE = 1
    private lateinit var viewModel: MedicineViewModel
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    val AVAILABILITY_CHANNEL_ID = "availability_channel"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear un canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.availability_channel_name)
            val descriptionText = getString(R.string.availability_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(AVAILABILITY_CHANNEL_ID, name, importance).apply {
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
        viewModel = ViewModelProvider(requireActivity()).get(MedicineViewModel::class.java)
        init(view)
        registerEvents()
        showGreeting()

        viewModel.buttonState.observe(viewLifecycleOwner) { state ->
            binding.pillButton.isEnabled = state.isPillButtonEnabled
            binding.phoneButton.isEnabled = state.isPhoneButtonEnabled
        }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
            userRef.child("cargo").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val cargo = dataSnapshot.getValue(String::class.java)
                    if (cargo != null) {
                        viewModel.updateButtonState(cargo)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    showErrorSnackbar("Error al obtener el cargo del usuario")
                }
            })
        }
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

    private fun showGreeting() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val databaseRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
            databaseRef.child("name").get().addOnSuccessListener { dataSnapshot ->
                val name = dataSnapshot.value.toString()
                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val greeting = when {
                    hour < 12 -> "Buenos días $name"
                    hour < 18 -> "Buenas tardes $name"
                    else -> "Buenas noches $name"
                }
                binding.greetingTextview.text = greeting
            }
        }
    }

    private fun registerEvents() {
        // Crear una animación para mostrar los puntos suspensivos uno por uno
        val dotAnimation = ObjectAnimator.ofInt(0, 4).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                val dotCount = animation.animatedValue as Int
                binding.tvLoggingOut.text = getString(R.string.Saliendo, ".".repeat(dotCount))
            }
        }

        binding.logoutButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Está seguro de que desea cerrar sesión?")
                .setPositiveButton("Sí") { dialog, which ->
                    // Mostrar el TextView y comenzar la animación
                    binding.tvLoggingOut.visibility = View.VISIBLE
                    dotAnimation.start()
                    // Cerrar sesión
                    auth.signOut()
                    // Navegar a la pantalla de inicio de sesión
                    navControl.navigate(R.id.action_homeFragment_to_signInFragment)
                    // Detener la animación y ocultar el TextView
                    dotAnimation.end()
                    binding.tvLoggingOut.visibility = View.GONE
                }
                .setNegativeButton("Cancelar") { dialog, which ->
                    // Cerrar el diálogo
                    dialog.dismiss()
                }
                .show()
        }


        binding.pillButton.setOnClickListener {
            if (binding.pillButton.isEnabled) {
                navControl.navigate(R.id.action_homeFragment_to_addMedicineFragment)
            } else {
                Snackbar.make(binding.root, "No tienes permitida esta acción", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnHelp.setOnClickListener {
            navControl.navigate(R.id.action_homeFragment_to_supportFragment)
        }


        binding.iconSearch.setOnClickListener {
            navControl.navigate(R.id.action_homeFragment_to_searchFragment)
        }

        val cardViews = listOf(binding.cardView1, binding.cardView2, binding.cardView3, binding.cardView4, binding.cardView5, binding.cardView6)
        val textViews = listOf(binding.categoryStomachache, binding.categoryHeadache, binding.categoryDizzy, binding.categoryWomensHealth, binding.categoryFirstAid, binding.categoryFlu)
        for (i in cardViews.indices) {
            val cardView = cardViews[i]
            val textView = textViews[i]
            cardView.setOnClickListener {
                val title = textView.text.toString()
                viewModel.selectedCategoryTitle.value = title
                // Navega al fragmento de destino
                navControl.navigate(R.id.action_homeFragment_to_medicineFragment)
            }
        }

        val database = FirebaseDatabase.getInstance()
        val phoneNumberRef = database.getReference("phoneNumber")

        phoneNumberRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val phoneNumber = dataSnapshot.getValue(String::class.java)
                if (phoneNumber != null) {
                    this@HomeFragment.phoneNumber = phoneNumber
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showErrorSnackbar("Error al obtener el número de teléfono")
            }
        })

        binding.btnDeleteAc.setOnClickListener {
            navControl.navigate(R.id.action_homeFragment_to_deleteAcFragment)
        }

        binding.phoneButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Llamada")
                .setMessage("¿Está seguro de marcar al número $phoneNumber?")
                .setNeutralButton("Editar") { dialog, which ->
                    // Editar el número
                    val editText = EditText(requireContext())
                    editText.inputType = InputType.TYPE_CLASS_PHONE
                    editText.filters = arrayOf(InputFilter.LengthFilter(12))
                    val editTextLayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    val margin = (16 * resources.displayMetrics.density).toInt()
                    editTextLayoutParams.setMargins(margin, 0, margin, 0)
                    editText.layoutParams = editTextLayoutParams

                    val characterCountTextView = TextView(requireContext())
                    characterCountTextView.text = getString(R.string.character_count, 0)
                    characterCountTextView.gravity = Gravity.END or Gravity.BOTTOM
                    val characterCountTextViewLayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    characterCountTextViewLayoutParams.setMargins(margin, 0, margin, 0)
                    characterCountTextView.layoutParams = characterCountTextViewLayoutParams

                    editText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            val characterCount = s?.length ?: 0
                            characterCountTextView.text = getString(R.string.character_count, characterCount)
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    })

                    val container = LinearLayout(requireContext())
                    container.orientation = LinearLayout.VERTICAL
                    container.addView(editText)
                    container.addView(characterCountTextView)

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Editar número")
                        .setView(container)
                        .setPositiveButton("Aceptar") { dialog, which ->
                            val newPhoneNumber = editText.text.toString()
                            if (newPhoneNumber.length in 7..12) {
                                phoneNumber = newPhoneNumber
                                phoneNumberRef.setValue(newPhoneNumber)
                                Snackbar.make(binding.root, "Número actualizado con éxito", Snackbar.LENGTH_LONG).show()
                            } else {
                                showErrorSnackbar("El número debe tener entre 7 y 12 dígitos")
                            }
                        }
                        .setNegativeButton("Cancelar") { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
                }
                .setNegativeButton("Cancelar") { dialog, which ->
                    // Cerrar el diálogo
                    dialog.dismiss()
                }
                .setPositiveButton("Sí") { dialog, which ->
                    // Realizar la llamada
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
                    } else {
                        val intent = Intent(Intent.ACTION_CALL)
                        intent.data = Uri.parse("tel:$phoneNumber")
                        startActivity(intent)
                    }
                }
                .show()
        }
    }
}