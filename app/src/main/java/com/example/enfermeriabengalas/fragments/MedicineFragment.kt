package com.example.enfermeriabengalas.fragments

import MedicineAdapter
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.FragmentMedicineBinding
import com.example.enfermeriabengalas.viewmodel.MedicineViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MedicineFragment : Fragment() {

    private lateinit var binding: FragmentMedicineBinding
    private lateinit var viewModel: MedicineViewModel
    private lateinit var navControl: NavController
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMedicineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MedicineViewModel::class.java)
        init(view)
        registerEvents()

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showErrorSnackbar(message)
        }
        // Create and set an adapter for the RecyclerView
        val adapter = MedicineAdapter(viewModel.medicines.value ?: emptyList())
        binding.medicinesRecyclerView.adapter = adapter
        binding.medicinesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe changes in the list of medicines and update the adapter
        viewModel.medicines.observe(viewLifecycleOwner) { medicines ->
            adapter.medicines = medicines
            adapter.notifyDataSetChanged()
        }
    }

    private fun init(view: View) {
        // Inicializa databaseRef con una referencia a la base de datos de Firebase
        databaseRef = FirebaseDatabase.getInstance().reference

        // Llama a la función init() de viewModel y pasa databaseRef como parámetro
        viewModel.init(databaseRef)

        viewModel.selectedCategoryTitle.observe(viewLifecycleOwner) { title ->
            // Replace line breaks with spaces
            val formattedTitle = title.replace("\n", " ")
            // Update the user interface with the title of the selected category
            binding.categoryTitle.text = formattedTitle

            // Call the getMedicines function to get the list of medicines in the selected category
            viewModel.getMedicines(formattedTitle)
        }
        navControl = Navigation.findNavController(view)
    }

    private fun registerEvents() {
        binding.backButton.setOnClickListener {
            navControl.navigate(R.id.action_medicineFragment_to_homeFragment)
        }
    }

    private fun showErrorSnackbar(message: String) {
        val contextView = view
        if (contextView != null) {
            val snackbarText = SpannableStringBuilder(message)
            snackbarText.setSpan(ForegroundColorSpan(Color.WHITE), 0, snackbarText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            snackbarText.setSpan(StyleSpan(Typeface.BOLD), 0, snackbarText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            Snackbar.make(contextView, snackbarText, Snackbar.LENGTH_SHORT).setBackgroundTint(
                Color.RED).show()
        }
    }
}
