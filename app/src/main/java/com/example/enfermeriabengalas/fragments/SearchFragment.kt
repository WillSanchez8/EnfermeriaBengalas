package com.example.enfermeriabengalas.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.adapters.MedicineAdapter
import com.example.enfermeriabengalas.adapters.MedicineAdapterListener
import com.example.enfermeriabengalas.databinding.FragmentSearchBinding
import com.example.enfermeriabengalas.models.Medicine
import com.example.enfermeriabengalas.viewmodel.MedicineViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase

class SearchFragment : Fragment() {

    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: MedicineViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MedicineViewModel::class.java)

        init(view)
        registerEvents()

        // Inicializa databaseRef con una referencia a la base de datos de Firebase
        val databaseRef = FirebaseDatabase.getInstance().reference

        // Llama a la función init() de viewModel y pasa databaseRef como parámetro
        viewModel.init(databaseRef)

        // Crea y asigna un adaptador para el RecyclerView
        val adapter = MedicineAdapter(emptyList(), object : MedicineAdapterListener {
            override fun onEditButtonClicked(medicine: Medicine) {
                // Establecer la propiedad medicineToEdit del ViewModel con el medicamento seleccionado
                viewModel.medicineToEdit.value = medicine
                // Navegar al fragmento AddMedicineFragment
                navControl.navigate(R.id.action_searchFragment_to_addMedicineFragment)
            }

            override fun onDeleteButtonClicked(medicine: Medicine) {
                // Mostrar un cuadro de diálogo para confirmar la eliminación del medicamento
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Eliminar medicamento")
                    .setMessage("¿Estás seguro de que deseas eliminar este medicamento?")
                    .setNegativeButton("Cancelar") { dialog, which ->
                        // No hacer nada si el usuario cancela la eliminación
                        dialog.dismiss()
                    }
                    .setPositiveButton("Sí") { dialog, which ->
                        // Llamar a la función deleteMedicine del ViewModel para eliminar el medicamento
                        viewModel.deleteMedicine(medicine)
                        Snackbar.make(binding.root, "Medicamento eliminado con éxito", Snackbar.LENGTH_SHORT).show()
                    }
                    .show()
            }
            override fun onPlusQuantityButtonClicked(medicine: Medicine) {
                viewModel.increaseMedicineQuantity(medicine)
            }

            override fun onMinusQuantityButtonClicked(medicine: Medicine) {
                viewModel.decreaseMedicineQuantity(medicine)
            }
        }, viewModel)
        binding.medicinesRecyclerView.adapter = adapter
        binding.medicinesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observa los cambios en los resultados de búsqueda y actualiza el adaptador
        viewModel.searchResults.observe(viewLifecycleOwner, Observer { searchResults ->
            adapter.medicines = searchResults
            adapter.notifyDataSetChanged()
        })
    }


    private fun init(view: View) {
        navControl = Navigation.findNavController(view)
    }

    private fun registerEvents() {

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Llama a la función searchMedicines cuando el usuario envía una consulta
                query?.let { viewModel.searchMedicines(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Llama a la función searchMedicines cuando el texto cambia
                newText?.let { viewModel.searchMedicines(it) }
                return true
            }
        })
    }

}