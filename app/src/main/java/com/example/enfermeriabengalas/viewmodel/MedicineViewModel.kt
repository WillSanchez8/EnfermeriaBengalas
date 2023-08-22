package com.example.enfermeriabengalas.viewmodel

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.enfermeriabengalas.models.Medicine
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.enfermeriabengalas.adapters.MyBroadcastReceiver
import com.example.enfermeriabengalas.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.Calendar
import java.util.UUID

class MedicineViewModel : ViewModel() {
    val selectedCategoryTitle = MutableLiveData<String>()
    val medicines = MutableLiveData<List<Medicine>>()
    val errorMessage = MutableLiveData<String>()
    private lateinit var databaseRef: DatabaseReference
    val medicineToEdit = MutableLiveData<Medicine?>()
    val searchResults = MutableLiveData<List<Medicine>>()
    val AVAILABILITY_CHANNEL_ID = "availability_channel"
    val AVAILABILITY_NOTIFICATION_ID = 2
    var lastCheckedDate: Calendar? = null
    //val buttonState = MutableLiveData<ButtonState>()

    fun init(databaseRef: DatabaseReference) {
        this.databaseRef = databaseRef
    }

    /*fun updateButtonState(cargo: String) {
        val disabledCargos = listOf("Alumnos/as", "Intendencia", "Profesores/as")
        val isEnabled = !disabledCargos.contains(cargo)
        buttonState.value = ButtonState(isEnabled, isEnabled, isEnabled, isEnabled, isEnabled, isEnabled)
    }*/

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

    fun searchMedicines(searchText: String) {
        val medicinesRef = databaseRef.child("medicines")
        medicinesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val medicinesList = mutableListOf<Medicine>()
                for (medicineSnapshot in snapshot.children) {
                    val medicine = medicineSnapshot.getValue(Medicine::class.java)
                    if (medicine != null && (medicine.name.contains(searchText) || medicine.category.contains(searchText) || medicine.description.contains(searchText))) {
                        medicinesList.add(medicine)
                    }
                }
                searchResults.value = medicinesList
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

    fun increaseMedicineQuantity(medicine: Medicine) {
        // Crear una copia del objeto medicine para evitar modificar el objeto original
        val newMedicine = medicine.copy()
        // Aumentar la cantidad del medicamento en 1 si es menor a 999
        if (newMedicine.quantity < 999) {
            newMedicine.quantity += 1
        }
        // Actualizar el medicamento en la base de datos
        updateMedicine(medicine, newMedicine, onSuccess = {
            // La actualización fue exitosa
        }, onFailure = { errorMessage ->
            // Hubo un error al actualizar el medicamento
            this.errorMessage.value = errorMessage
        })
    }

    fun decreaseMedicineQuantity(medicine: Medicine) {
        // Crear una copia del objeto medicine para evitar modificar el objeto original
        val newMedicine = medicine.copy()
        // Disminuir la cantidad del medicamento en 1 si es mayor a 0
        if (newMedicine.quantity > 0) {
            newMedicine.quantity -= 1
        }
        // Actualizar el medicamento en la base de datos
        updateMedicine(medicine, newMedicine, onSuccess = {
            // La actualización fue exitosa
        }, onFailure = { errorMessage ->
            // Hubo un error al actualizar el medicamento
            this.errorMessage.value = errorMessage
        })
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
                    // Asigna el valor de imageUri a una variable local
                    val localImageUri = newMedicine.image?.let { Uri.parse(it) }

                    // Comprueba si localImageUri no es nula y si es una URI local (no una URL de Firebase Storage)
                    if (localImageUri != null && localImageUri.scheme == "content") {
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
                                    // Actualiza la propiedad image del objeto newMedicine con la URL de descarga de la imagen
                                    newMedicine.image = downloadUrl.toString()

                                    // Actualizar los datos del medicamento en la base de datos
                                    medicineSnapshot.ref.setValue(newMedicine)
                                        .addOnSuccessListener {
                                            onSuccess()
                                        }
                                        .addOnFailureListener {
                                            onFailure("Error al actualizar el medicamento")
                                        }
                                }
                            }
                            .addOnFailureListener {
                                onFailure("Error al subir la imagen")
                            }
                    } else {
                        // localImageUri es nula o es una URL de Firebase Storage
                        // Actualiza los datos del medicamento en la base de datos sin subir nuevamente la imagen

                        // Actualizar los datos del medicamento en la base de datos
                        medicineSnapshot.ref.setValue(newMedicine)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener {
                                onFailure("Error al actualizar el medicamento")
                            }
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

    fun checkMedicineAvailability(context: Context) {
        val currentDate = Calendar.getInstance()
        if (lastCheckedDate == null || currentDate.after(lastCheckedDate)) {
            val databaseRef = FirebaseDatabase.getInstance().reference.child("medicines")
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var lowAvailabilityCount = 0
                    var highAvailabilityCount = 0
                    for (medicineSnapshot in snapshot.children) {
                        val medicine = medicineSnapshot.getValue(Medicine::class.java)
                        when {
                            medicine != null && medicine.quantity < 5 -> lowAvailabilityCount++
                            medicine != null && medicine.quantity >= 5 -> highAvailabilityCount++
                        }
                    }
                    if (lowAvailabilityCount >= 5) {
                        // Hay muchos medicamentos con poca disponibilidad
                        // Notificar al usuario
                        sendLowAvailabilityNotification(context)
                    } else if (highAvailabilityCount >= 5) {
                        // Hay suficientes medicamentos disponibles
                        // Notificar al usuario
                        sendHighAvailabilityNotification(context)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    errorMessage.value = "Ocurrió un error al intentar obtener los datos de medicamentos. Por favor, inténtalo de nuevo más tarde."
                }
            })
            lastCheckedDate = currentDate
        }
    }

    private fun sendLowAvailabilityNotification(context: Context) {
        // Obtener el nombre del paquete de la aplicación
        val packageName = context.packageName
        // Crear una notificación para mostrar al usuario
        val builder = NotificationCompat.Builder(context, AVAILABILITY_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_tiger_sad)
            .setContentTitle("Poca disponibilidad de medicamentos")
            .setContentText("Hay muchos medicamentos con poca disponibilidad 😔. Por favor, revisa la disponibilidad de los productos para evitar que se agoten.")
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
            notify(AVAILABILITY_NOTIFICATION_ID, builder.build())
        }
    }

    private fun sendHighAvailabilityNotification(context: Context) {
        // Obtener el nombre del paquete de la aplicación
        val packageName = context.packageName
        // Crear una notificación para mostrar al usuario
        val builder = NotificationCompat.Builder(context, AVAILABILITY_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_tiger)
            .setContentTitle("Suficientes medicamentos disponibles")
            .setContentText("Aún cuentas con una gran variedad de medicamentos disponibles 😊.")
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
            notify(AVAILABILITY_NOTIFICATION_ID, builder.build())
        }
    }
}
/*
data class ButtonState(
    val isPillButtonEnabled: Boolean,
    val isPhoneButtonEnabled: Boolean,
    val isEditMedicineEnabled: Boolean,
    val isDeleteMedicineEnabled: Boolean,
    val isPlusQuantityButtonEnabled: Boolean,
    val isMinusQuantityButtonEnabled: Boolean
)*/