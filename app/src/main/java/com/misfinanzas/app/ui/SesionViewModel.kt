package com.misfinanzas.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.misfinanzas.app.data.AppDatabase
import com.misfinanzas.app.data.Cuenta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class EstadoSesion { REGISTRO, LOGIN, DENTRO }

class SesionViewModel(app: Application) : AndroidViewModel(app) {
    private val cuenta = Cuenta(app)

    private val _estado = MutableStateFlow(calcularEstado())
    val estado: StateFlow<EstadoSesion> = _estado.asStateFlow()

    val nombre: String get() = cuenta.nombre
    val usuario: String get() = cuenta.usuario

    private fun calcularEstado() = when {
        !cuenta.existe -> EstadoSesion.REGISTRO
        cuenta.sesionIniciada -> EstadoSesion.DENTRO
        else -> EstadoSesion.LOGIN
    }

    /** Devuelve un mensaje de error, o null si el registro fue exitoso. */
    fun registrar(nombre: String, usuario: String, contrasena: String, confirmacion: String): String? {
        val error = when {
            nombre.isBlank() -> "Escribe tu nombre."
            usuario.isBlank() -> "Escribe un nombre de usuario."
            usuario.trim().contains(' ') -> "El usuario no puede tener espacios."
            contrasena.length < 4 -> "La contraseña debe tener al menos 4 caracteres."
            contrasena != confirmacion -> "Las contraseñas no coinciden."
            else -> null
        }
        if (error == null) {
            cuenta.registrar(nombre, usuario, contrasena)
            _estado.value = EstadoSesion.DENTRO
        }
        return error
    }

    fun iniciarSesion(usuario: String, contrasena: String): String? {
        if (usuario.isBlank() || contrasena.isEmpty()) return "Escribe tu usuario y contraseña."
        if (!cuenta.iniciarSesion(usuario, contrasena)) return "Usuario o contraseña incorrectos."
        _estado.value = EstadoSesion.DENTRO
        return null
    }

    fun cerrarSesion() {
        cuenta.cerrarSesion()
        _estado.value = EstadoSesion.LOGIN
    }

    /** Olvidé mi contraseña: borra la cuenta y todos los movimientos para empezar de cero. */
    fun reiniciarTodo() {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.get(getApplication()).clearAllTables()
            cuenta.borrar()
            _estado.value = EstadoSesion.REGISTRO
        }
    }
}
