package com.example.triviatrainerapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RespuestaUsuario(
    val idPregunta: Int,
    val indiceSeleccionado: Int,
    val indiceCorrecto: Int
) : Parcelable
