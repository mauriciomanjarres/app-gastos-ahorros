package com.misfinanzas.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.misfinanzas.app.data.AppDatabase
import com.misfinanzas.app.data.Movimiento
import com.misfinanzas.app.data.TipoMovimiento
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class Resumen(
    val ingresos: Double = 0.0,
    val gastos: Double = 0.0,
    val ahorros: Double = 0.0,
    val retiros: Double = 0.0,
) {
    /** Dinero libre: lo que entra menos lo que se gasta y lo que se aparta para ahorro. */
    val disponible: Double get() = ingresos - gastos - ahorros + retiros
    val ahorroNeto: Double get() = ahorros - retiros

    companion object {
        fun de(totales: Map<TipoMovimiento, Double>) = Resumen(
            ingresos = totales[TipoMovimiento.INGRESO] ?: 0.0,
            gastos = totales[TipoMovimiento.GASTO] ?: 0.0,
            ahorros = totales[TipoMovimiento.AHORRO] ?: 0.0,
            retiros = totales[TipoMovimiento.RETIRO_AHORRO] ?: 0.0,
        )
    }
}

data class EstadoMes(
    val movimientos: List<Movimiento> = emptyList(),
    val resumen: Resumen = Resumen(),
    val gastosPorCategoria: List<Pair<String, Double>> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class FinanzasViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).movimientoDao()

    private val _mes = MutableStateFlow(YearMonth.now())
    val mes: StateFlow<YearMonth> = _mes.asStateFlow()

    val estadoMes: StateFlow<EstadoMes> = _mes
        .flatMapLatest { m -> dao.delPeriodo(m.atDay(1).toEpochDay(), m.atEndOfMonth().toEpochDay()) }
        .map { lista ->
            EstadoMes(
                movimientos = lista,
                resumen = Resumen.de(
                    lista.groupBy { it.tipo }.mapValues { (_, v) -> v.sumOf { it.monto } }
                ),
                gastosPorCategoria = lista
                    .filter { it.tipo == TipoMovimiento.GASTO }
                    .groupBy { it.categoria }
                    .map { (cat, v) -> cat to v.sumOf { it.monto } }
                    .sortedByDescending { it.second },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoMes())

    /** Totales de todos los meses: balance general acumulado. */
    val historico: StateFlow<Resumen> = dao.totalesHistoricos()
        .map { lista -> Resumen.de(lista.associate { it.tipo to it.total }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resumen())

    fun mesAnterior() = _mes.update { it.minusMonths(1) }
    fun mesSiguiente() = _mes.update { it.plusMonths(1) }

    fun guardar(movimiento: Movimiento) = viewModelScope.launch { dao.guardar(movimiento) }
    fun eliminar(movimiento: Movimiento) = viewModelScope.launch { dao.eliminar(movimiento) }
}
