package com.misfinanzas.app.ui

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Lo que la pantalla de acceso necesita mostrar. */
data class EstadoAcceso(
    val cargando: Boolean = false,
    val error: String? = null,
    val aviso: String? = null,
)

class SesionViewModel(app: Application) : AndroidViewModel(app) {
    private val auth = FirebaseAuth.getInstance().apply { setLanguageCode("es") }

    private val _usuario = MutableStateFlow(auth.currentUser)
    /** null = no hay sesión iniciada. Firebase recuerda la sesión entre aperturas de la app. */
    val usuario: StateFlow<FirebaseUser?> = _usuario.asStateFlow()

    private val _acceso = MutableStateFlow(EstadoAcceso())
    val acceso: StateFlow<EstadoAcceso> = _acceso.asStateFlow()

    fun limpiarMensajes() {
        _acceso.value = _acceso.value.copy(error = null, aviso = null)
    }

    fun registrar(nombre: String, correo: String, contrasena: String, confirmacion: String) {
        val error = when {
            nombre.isBlank() -> "Escribe tu nombre."
            !correoValido(correo) -> "Escribe un correo válido."
            contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            contrasena != confirmacion -> "Las contraseñas no coinciden."
            else -> null
        }
        if (error != null) {
            _acceso.value = EstadoAcceso(error = error)
            return
        }
        ejecutar {
            val usuario = auth.createUserWithEmailAndPassword(correo.trim(), contrasena).await().user
            usuario?.updateProfile(userProfileChangeRequest { displayName = nombre.trim() })?.await()
            usuario?.reload()?.await()
            _usuario.value = auth.currentUser
        }
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        if (!correoValido(correo) || contrasena.isEmpty()) {
            _acceso.value = EstadoAcceso(error = "Escribe tu correo y tu contraseña.")
            return
        }
        ejecutar {
            auth.signInWithEmailAndPassword(correo.trim(), contrasena).await()
            _usuario.value = auth.currentUser
        }
    }

    fun recuperarContrasena(correo: String) {
        if (!correoValido(correo)) {
            _acceso.value = EstadoAcceso(error = "Escribe el correo con el que te registraste.")
            return
        }
        ejecutar {
            auth.sendPasswordResetEmail(correo.trim()).await()
            _acceso.value = EstadoAcceso(
                aviso = "Te enviamos un enlace a ${correo.trim()} para crear una contraseña nueva. " +
                    "Revisa también la carpeta de spam."
            )
        }
    }

    fun cerrarSesion() {
        auth.signOut()
        _acceso.value = EstadoAcceso()
        _usuario.value = null
    }

    private fun ejecutar(accion: suspend () -> Unit) {
        if (_acceso.value.cargando) return
        _acceso.value = EstadoAcceso(cargando = true)
        viewModelScope.launch {
            try {
                accion()
                if (_acceso.value.cargando) _acceso.value = EstadoAcceso()
            } catch (e: Exception) {
                _acceso.value = EstadoAcceso(error = mensajeDe(e))
            }
        }
    }

    private fun correoValido(correo: String) = Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()

    private fun mensajeDe(e: Exception): String = when (e) {
        is FirebaseNetworkException -> "No hay conexión a internet. Revisa tu conexión e inténtalo de nuevo."
        is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera unos minutos e inténtalo de nuevo."
        is FirebaseAuthException -> when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo no es válido."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con ese correo. Inicia sesión."
            "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil. Usa al menos 6 caracteres."
            "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL" ->
                "Correo o contraseña incorrectos."
            "ERROR_USER_DISABLED" -> "Esta cuenta fue deshabilitada."
            "ERROR_OPERATION_NOT_ALLOWED" ->
                "El inicio de sesión con correo no está activado en Firebase."
            else -> "No se pudo completar: ${e.localizedMessage}"
        }
        else -> "Ocurrió un error: ${e.localizedMessage ?: "inténtalo de nuevo"}"
    }
}
