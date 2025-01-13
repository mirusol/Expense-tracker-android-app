package com.example.lab1.database

data class Transaction(
    val id: Int,
    var amount: Double,
    var description: String,
    var date: Long,
    var category: String
)