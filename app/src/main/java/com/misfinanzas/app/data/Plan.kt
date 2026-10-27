package com.misfinanzas.app.data

import android.content.Context

/** Cómo se calcula el ahorro mensual a partir del sueldo. */
enum class ModoAhorro { PORCENTAJE, MONTO }

/** Plan mensual del usuario: sueldo base, cuánto ahorrar y su meta de ahorro. */
data class Plan(
    val sueldo: Double = 0.0,
    val modoAhorro: ModoAhorro = ModoAhorro.PORCENTAJE,
    /** Porcentaje (0-100) o monto fijo según [modoAhorro]. */
    val valorAhorro: Double = 10.0,
    val nombreMeta: String = "",
    val montoMeta: Double = 0.0,
) {
    val ahorroMensual: Double
        get() = when (modoAhorro) {
            ModoAhorro.PORCENTAJE -> sueldo * valorAhorro.coerceIn(0.0, 100.0) / 100.0
            ModoAhorro.MONTO -> valorAhorro.coerceIn(0.0, sueldo.coerceAtLeast(0.0))
        }

    /** Lo que queda del sueldo después de apartar el ahorro. */
    val quedaParaGastar: Double get() = sueldo - ahorroMensual

    val porcentajeAhorro: Double get() = if (sueldo > 0) ahorroMensual / sueldo * 100 else 0.0
}

/** Guarda el plan en el teléfono, uno por cada cuenta. */
class PlanStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("plan", Context.MODE_PRIVATE)

    fun leer(uid: String): Plan = Plan(
        sueldo = prefs.leerNumero("$uid.sueldo", 0.0),
        modoAhorro = runCatching { ModoAhorro.valueOf(prefs.getString("$uid.modo", null) ?: "") }
            .getOrDefault(ModoAhorro.PORCENTAJE),
        valorAhorro = prefs.leerNumero("$uid.valor", 10.0),
        nombreMeta = prefs.getString("$uid.nombreMeta", "") ?: "",
        montoMeta = prefs.leerNumero("$uid.montoMeta", 0.0),
    )

    fun guardar(uid: String, plan: Plan) {
        prefs.edit()
            .putString("$uid.sueldo", plan.sueldo.toString())
            .putString("$uid.modo", plan.modoAhorro.name)
            .putString("$uid.valor", plan.valorAhorro.toString())
            .putString("$uid.nombreMeta", plan.nombreMeta)
            .putString("$uid.montoMeta", plan.montoMeta.toString())
            .apply()
    }

    // Los números se guardan como texto para no perder precisión con montos grandes.
    private fun android.content.SharedPreferences.leerNumero(clave: String, defecto: Double) =
        getString(clave, null)?.toDoubleOrNull() ?: defecto
}
