package com.example.triviatrainerapp

data class Pregunta(
    val id: Int,
    val pregunta: String,
    val opciones: List<String>,
    val respuesta_correcta: Int,
    val fuente: String
)
