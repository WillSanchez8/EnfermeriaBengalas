package com.example.enfermeriabengalas.adapters

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.example.enfermeriabengalas.viewmodel.MedicineViewModel

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Obtener una referencia al ViewModel
        val viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application).create(
            MedicineViewModel::class.java)

        // Llamar a la función checkMedicineAvailability del ViewModel para verificar la disponibilidad de los medicamentos
        viewModel.checkMedicineAvailability(context)
    }
}
