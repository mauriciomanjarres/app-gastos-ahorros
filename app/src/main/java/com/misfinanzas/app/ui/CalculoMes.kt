package com.misfinanzas.app.ui

import com.misfinanzas.app.data.Movimiento
import com.misfinanzas.app.data.Plan
import com.misfinanzas.app.data.TipoMovimiento

/**
 * Cuánto dinero queda en el mes, partiendo del sueldo del plan.
 *
 * Si hay sueldo en el plan se usa ese valor como sueldo del mes (y los ingresos de categoría
 * "Salario" se ignoran para no contarlo dos veces). Sin plan, se usan los ingresos registrados.
 */
data class CalculoMes(
    val sueldo: Double,
    val otrosIngresos: Double,
    val gastos: Double,
    val ahorro: Double,
    val retiros: Double,
    val desdePlan: Boolean,
) {
    val totalIngresos: Double get() = sueldo + otrosIngresos
    private val saldo: Double get() = totalIngresos - gastos - ahorro + retiros

    /** Lo que queda para gastar; nunca es negativo. */
    val disponible: Double get() = saldo.coerceAtLeast(0.0)

    /** Cuánto se gastó por encima de lo que había (0 si no se pasó). */
    val excedido: Double get() = (-saldo).coerceAtLeast(0.0)

    /** Fracción del dinero del mes que ya se usó (gastos + ahorro), de 0 a 1. */
    val fraccionUsada: Double
        get() = if (totalIngresos > 0) ((gastos + ahorro - retiros) / totalIngresos).coerceIn(0.0, 1.0) else 0.0

    companion object {
        fun de(plan: Plan, movimientos: List<Movimiento>): CalculoMes {
            fun suma(tipo: TipoMovimiento, filtro: (Movimiento) -> Boolean = { true }) =
                movimientos.filter { it.tipo == tipo && filtro(it) }.sumOf { it.monto }

            val gastos = suma(TipoMovimiento.GASTO)
            val ahorrado = suma(TipoMovimiento.AHORRO)
            val retiros = suma(TipoMovimiento.RETIRO_AHORRO)
            return if (plan.sueldo > 0) {
                CalculoMes(
                    sueldo = plan.sueldo,
                    otrosIngresos = suma(TipoMovimiento.INGRESO) { it.categoria != "Salario" },
                    gastos = gastos,
                    ahorro = maxOf(ahorrado, plan.ahorroMensual),
                    retiros = retiros,
                    desdePlan = true,
                )
            } else {
                CalculoMes(
                    sueldo = suma(TipoMovimiento.INGRESO),
                    otrosIngresos = 0.0,
                    gastos = gastos,
                    ahorro = ahorrado,
                    retiros = retiros,
                    desdePlan = false,
                )
            }
        }
    }
}
