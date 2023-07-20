package com.example.enfermeriabengalas.models

//Deserialzacion de datos de la base de datos
class Medicine(
    var name: String = "",
    var description: String = "",
    var quantity: Int = 0,
    var category: String = "",
    var image: String? = null
)
