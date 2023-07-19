package com.example.enfermeriabengalas.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.FragmentMedicineBinding
import com.example.enfermeriabengalas.viewmodel.MedicineViewModel

class MedicineFragment : Fragment() {

    private lateinit var binding: FragmentMedicineBinding
    private lateinit var viewModel: MedicineViewModel
    private lateinit var navControl: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentMedicineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MedicineViewModel::class.java)
        init(view)
        registerEvents()

        // Crea y establece un adaptador para el RecyclerView
      /*  val adapter = MyAdapter()
        binding.medicinesRecyclerView.adapter = adapter*/
    }

    private fun init(view: View) {
        viewModel.selectedCategoryTitle.observe(viewLifecycleOwner) { title ->
            // Reemplaza los saltos de línea con espacios
            val formattedTitle = title.replace("\n", " ")
            // Actualiza la interfaz de usuario con el título de la categoría seleccionada
            binding.categoryTitle.text = formattedTitle
        }
        navControl = Navigation.findNavController(view)
    }

    private fun registerEvents() {
        binding.backButton.setOnClickListener {
            navControl.navigate(R.id.action_medicineFragment_to_homeFragment)
        }
    }

}
