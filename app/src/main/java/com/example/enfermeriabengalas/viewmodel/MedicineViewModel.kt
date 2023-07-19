package com.example.enfermeriabengalas.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MedicineViewModel : ViewModel() {
    val selectedCategoryTitle = MutableLiveData<String>()
    //val medicines = MutableLiveData<List<Medicine>>()
}
