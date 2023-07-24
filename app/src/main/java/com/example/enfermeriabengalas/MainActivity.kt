package com.example.enfermeriabengalas

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.enfermeriabengalas.viewmodel.MedicineViewModel
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtener una instancia del ViewModel
        val viewModel = ViewModelProvider(this).get(MedicineViewModel::class.java)

        // Obtener la fecha y hora en que se ejecutó la función por última vez
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val lastCheckedTime = sharedPreferences.getLong("lastCheckedTime", 0)

        /*SharedPreferences sirve para guardar datos en el dispositivo de forma persistente (aunque se cierre la aplicación)
        y poder acceder a ellos desde cualquier parte de la aplicación (a diferencia de los Bundle, que solo sirven para pasar datos entre actividades)
         */
        // Verificar si ha pasado suficiente tiempo desde la última vez que se ejecutó la función
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCheckedTime > 24 * 60 * 60 * 1000) {
            // Llamar a la función checkMedicineAvailability del ViewModel para verificar la disponibilidad de los medicamentos
            viewModel.checkMedicineAvailability(this)

            // Actualizar el valor de lastCheckedTime en SharedPreferences
            with(sharedPreferences.edit()) {
                putLong("lastCheckedTime", currentTime)
                apply()
            }
        }
    }
}


