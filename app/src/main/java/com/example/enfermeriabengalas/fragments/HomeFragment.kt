package com.example.enfermeriabengalas.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentHomeBinding

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

        init(view)
        registerEvents()
        showGreeting()
    }

    private fun init(view: View) {
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
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
    }
}