package com.misfinanzas.app.data

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.util.Base64

/**
 * Cuenta local del dueño del teléfono. La contraseña nunca se guarda: solo un hash PBKDF2 con sal.
 */
class Cuenta(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("cuenta", Context.MODE_PRIVATE)

    val existe: Boolean get() = prefs.contains(CLAVE_HASH)
    val nombre: String get() = prefs.getString(CLAVE_NOMBRE, "") ?: ""
    val usuario: String get() = prefs.getString(CLAVE_USUARIO, "") ?: ""
    val sesionIniciada: Boolean get() = existe && prefs.getBoolean(CLAVE_SESION, false)

    fun registrar(nombre: String, usuario: String, contrasena: String) {
        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(CLAVE_NOMBRE, nombre.trim())
            .putString(CLAVE_USUARIO, usuario.trim().lowercase())
            .putString(CLAVE_SAL, Base64.encodeToString(sal, Base64.NO_WRAP))
            .putString(CLAVE_HASH, Base64.encodeToString(hash(contrasena, sal), Base64.NO_WRAP))
            .putBoolean(CLAVE_SESION, true)
            .apply()
    }

    /** Devuelve true si el usuario y la contraseña son correctos (y deja la sesión iniciada). */
    fun iniciarSesion(usuario: String, contrasena: String): Boolean {
        val sal = Base64.decode(prefs.getString(CLAVE_SAL, null) ?: return false, Base64.NO_WRAP)
        val guardado = Base64.decode(prefs.getString(CLAVE_HASH, null) ?: return false, Base64.NO_WRAP)
        val ok = usuario.trim().lowercase() == this.usuario &&
            MessageDigest.isEqual(hash(contrasena, sal), guardado)
        if (ok) prefs.edit().putBoolean(CLAVE_SESION, true).apply()
        return ok
    }

    fun cerrarSesion() = prefs.edit().putBoolean(CLAVE_SESION, false).apply()

    fun borrar() = prefs.edit().clear().apply()

    private fun hash(contrasena: String, sal: ByteArray): ByteArray {
        val spec = PBEKeySpec(contrasena.toCharArray(), sal, 10_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private companion object {
        const val CLAVE_NOMBRE = "nombre"
        const val CLAVE_USUARIO = "usuario"
        const val CLAVE_SAL = "sal"
        const val CLAVE_HASH = "hash"
        const val CLAVE_SESION = "sesion"
    }
}
