package com.example.triviatrainerapp

class Usuario {
    var username: String = ""
    var password: String = ""
    var email: String = ""
    var clave: String = ""


    constructor(username: String, password: String, email: String, clave: String ){
        this.username = username
        this.email = email
        this.password = password
        this.clave = clave
    }
    constructor() {
        // Inicializa las propiedades si es necesario, aunque con 'var' y valores por defecto arriba,
        // Firebase se encargará de llenarlas.
    }
}