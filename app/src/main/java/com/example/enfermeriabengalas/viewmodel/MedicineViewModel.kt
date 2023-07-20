package com.example.enfermeriabengalas.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.enfermeriabengalas.models.Medicine
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class MedicineViewModel : ViewModel() {
    val selectedCategoryTitle = MutableLiveData<String>()
    val medicines = MutableLiveData<List<Medicine>>()
    val errorMessage = MutableLiveData<String>()
    private lateinit var databaseRef: DatabaseReference
    val medicineToEdit = MutableLiveData<Medicine?>()

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

    fun deleteMedicine(medicine: Medicine) {
// Eliminar el medicamento de la base de datos
        val medicinesRef = databaseRef.child("medicines")
        val medicineQuery = medicinesRef.orderByChild("name").equalTo(medicine.name)
        medicineQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (medicineSnapshot in snapshot.children) {
                    medicineSnapshot.ref.removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // El medicamento se eliminó correctamente de la base de datos
                        } else {
                            errorMessage.value = "Ocurrió un error al intentar eliminar el medicamento de la base de datos. Por favor, inténtalo de nuevo más tarde."
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                errorMessage.value = "Ocurrió un error al intentar eliminar el medicamento. Por favor, inténtalo de nuevo más tarde."
            }
        })

        // Eliminar la imagen del medicamento del almacenamiento de Firebase
        if (medicine.image != null) {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(medicine.image!!)
            storageRef.delete().addOnSuccessListener {
                // La imagen fue eliminada con éxito del almacenamiento de Firebase
            }.addOnFailureListener {
                errorMessage.value = "Ocurrió un error al intentar eliminar la imagen del medicamento del almacenamiento de Firebase. Por favor, inténtalo de nuevo más tarde."
            }
        }
    }

    fun addMedicine(medicine: Medicine, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Asigna el valor de imageUri a una variable local
        val localImageUri = medicine.image?.let { Uri.parse(it) }

        // Comprueba si localImageUri no es nula
        if (localImageUri != null) {
            // Genera un nombre de archivo único utilizando un UUID
            val fileName = "imagen_${UUID.randomUUID()}.jpg"
            // Crea una referencia a la ruta completa del archivo en Firebase Storage
            val storageRef = Firebase.storage.reference.child("imagenes/$fileName")
            // Sube el archivo a Firebase Storage
            storageRef.putFile(localImageUri)
                .addOnSuccessListener {
                    // El archivo se subió correctamente
                    // Obtiene la URL de descarga del archivo
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        // Actualiza la propiedad image del objeto medicine con la URL de descarga de la imagen
                        medicine.image = downloadUrl.toString()

                        // Guarda los datos del medicamento en una ruta global accesible para todos los usuarios
                        databaseRef.child("medicines").push().setValue(medicine)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener {
                                onFailure("Error al agregar el medicamento")
                            }
                    }
                }
                .addOnFailureListener {
                    onFailure("Error al subir la imagen")
                }
        } else {
            // localImageUri es nula
            // Guarda los datos del medicamento en la base de datos sin incluir la URL de descarga de la imagen

            // Guarda los datos del medicamento en una ruta global accesible para todos los usuarios
            databaseRef.child("medicines").push().setValue(medicine)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener {
                    onFailure("Error al agregar el medicamento")
                }
        }
    }

    fun updateMedicine(medicine: Medicine, newMedicine: Medicine, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Obtener la referencia al nodo de medicamentos en la base de datos
        val medicinesRef = databaseRef.child("medicines")

        // Consultar la base de datos para obtener el medicamento a editar
        val medicineQuery = medicinesRef.orderByChild("name").equalTo(medicine.name)

        // Escuchar cambios en los resultados de la consulta
        medicineQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Obtener el primer resultado de la consulta (debería haber solo uno)
                val medicineSnapshot = snapshot.children.firstOrNull()

                if (medicineSnapshot != null) {
                    // Actualizar los datos del medicamento en la base de datos
                    medicineSnapshot.ref.setValue(newMedicine)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener {
                            onFailure("Error al actualizar el medicamento")
                        }
                } else {
                    onFailure("No se encontró el medicamento a editar")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure("Error al intentar obtener los datos del medicamento")
            }
        })
    }
}
