package com.example.kingsoftlog

data class Product(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int = 0,
    val categoryId: Int? = null,
    val imageUrl: String? = null
)
