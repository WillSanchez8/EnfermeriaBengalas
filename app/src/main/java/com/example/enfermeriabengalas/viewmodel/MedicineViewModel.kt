package com.example.enfermeriabengalas.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.enfermeriabengalas.models.Medicine
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import android.util.Log

class MedicineViewModel : ViewModel() {
    val selectedCategoryTitle = MutableLiveData<String>()
    val medicines = MutableLiveData<List<Medicine>>()
    val errorMessage = MutableLiveData<String>()
    private lateinit var databaseRef: DatabaseReference

    fun init(databaseRef: DatabaseReference) {
        this.databaseRef = databaseRef
    }

    fun getMedicines(categoryTitle: String) {
        // Get the reference to the medicines node in the database
        val medicinesRef = databaseRef.child("medicines")

        // Query the database for medicines in the specified category
        val medicinesQuery = medicinesRef.orderByChild("category").equalTo(categoryTitle)

        // Listen for changes to the query results
        medicinesQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get the list of medicines from the query results
                // Get the list of medicines from the query results
                val medicinesList = mutableListOf<Medicine>()
                for (medicineSnapshot in snapshot.children) {
                    val medicine = medicineSnapshot.getValue(Medicine::class.java)
                    medicine?.let { medicinesList.add(it) }
                }
                // Update the medicines list in the viewmodel
                medicines.value = medicinesList
            }

            override fun onCancelled(error: DatabaseError) {
                errorMessage.value = "Ocurrió un error al intentar obtener los datos de medicamentos. Por favor, inténtalo de nuevo más tarde."
            }
        })
    }


}
