package com.example.enfermeriabengalas.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MedicineViewModel::class.java)
        init(view)
        registerEvents()
        showGreeting()
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
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            navControl.navigate(R.id.action_homeFragment_to_signInFragment)
        }

        binding.pillButton.setOnClickListener {
            navControl.navigate(R.id.action_homeFragment_to_addMedicineFragment)
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
                // Maneja el error
            }
        })

        binding.phoneButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Llamada")
                .setMessage("¿Está seguro de marcar al número $phoneNumber?")
                .setNeutralButton("Editar") { dialog, which ->
                    // Editar el número
                    val editText = EditText(requireContext())
                    editText.inputType = InputType.TYPE_CLASS_PHONE
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Editar número")
                        .setView(editText)
                        .setPositiveButton("Aceptar") { dialog, which ->
                            val newPhoneNumber = editText.text.toString()
                            if (newPhoneNumber.length in 7..12) {
                                phoneNumber = newPhoneNumber
                                phoneNumberRef.setValue(newPhoneNumber)
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