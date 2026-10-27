package com.misfinanzas.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.misfinanzas.app.data.AppDatabase
import com.misfinanzas.app.data.Movimiento
import com.misfinanzas.app.data.Plan
import com.misfinanzas.app.data.PlanStore
import com.misfinanzas.app.data.TotalPorCategoria
import com.misfinanzas.app.data.TipoMovimiento
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
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

const val DESCRIPCION_SUELDO = "Sueldo base"
const val DESCRIPCION_AHORRO = "Ahorro del sueldo"

@OptIn(ExperimentalCoroutinesApi::class)
class FinanzasViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).movimientoDao()
    private val planStore = PlanStore(app)

    private val _plan = MutableStateFlow(Plan())
    val plan: StateFlow<Plan> = _plan.asStateFlow()

    private val _mes = MutableStateFlow(YearMonth.now())
    val mes: StateFlow<YearMonth> = _mes.asStateFlow()

    private val usuarioId = MutableStateFlow("")

    /** Cambia los datos mostrados a los del usuario que inició sesión. */
    fun usarCuenta(uid: String) {
        if (usuarioId.value == uid) return
        viewModelScope.launch {
            dao.adoptarSinDueno(uid)
            _plan.value = planStore.leer(uid)
            usuarioId.value = uid
        }
    }

    val estadoMes: StateFlow<EstadoMes> = combine(usuarioId, _mes) { uid, m -> uid to m }
        .flatMapLatest { (uid, m) -> dao.delPeriodo(uid, m.atDay(1).toEpochDay(), m.atEndOfMonth().toEpochDay()) }
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
    val historico: StateFlow<Resumen> = usuarioId
        .flatMapLatest { dao.totalesHistoricos(it) }
        .map { lista -> Resumen.de(lista.associate { it.tipo to it.total }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resumen())

    val ahorrosPorCategoria: StateFlow<List<TotalPorCategoria>> = usuarioId
        .flatMapLatest { dao.ahorrosPorCategoria(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun guardarPlan(plan: Plan) {
        _plan.value = plan
        if (usuarioId.value.isNotEmpty()) planStore.guardar(usuarioId.value, plan)
    }

    /**
     * Registra (o actualiza) en el mes visto el ingreso del sueldo y el ahorro del plan,
     * para que el resumen y el balance los tengan en cuenta.
     */
    fun registrarSueldoDelMes(plan: Plan) = viewModelScope.launch {
        val m = _mes.value
        val hoy = LocalDate.now()
        val fecha = (if (YearMonth.from(hoy) == m) hoy else m.atDay(1)).toEpochDay()
        val delMes = estadoMes.value.movimientos
        val sueldoPrevio = delMes.firstOrNull { it.descripcion == DESCRIPCION_SUELDO }
        val ahorroPrevio = delMes.firstOrNull { it.descripcion == DESCRIPCION_AHORRO }
        dao.guardar(
            Movimiento(
                id = sueldoPrevio?.id ?: 0,
                tipo = TipoMovimiento.INGRESO,
                monto = plan.sueldo,
                categoria = "Salario",
                descripcion = DESCRIPCION_SUELDO,
                fecha = sueldoPrevio?.fecha ?: fecha,
                usuarioId = usuarioId.value,
            )
        )
        if (plan.ahorroMensual > 0) {
            dao.guardar(
                Movimiento(
                    id = ahorroPrevio?.id ?: 0,
                    tipo = TipoMovimiento.AHORRO,
                    monto = plan.ahorroMensual,
                    categoria = ahorroPrevio?.categoria ?: "Meta personal",
                    descripcion = DESCRIPCION_AHORRO,
                    fecha = ahorroPrevio?.fecha ?: fecha,
                    usuarioId = usuarioId.value,
                )
            )
        } else if (ahorroPrevio != null) {
            dao.eliminar(ahorroPrevio)
        }
    }

    fun mesAnterior() = _mes.update { it.minusMonths(1) }
    fun mesSiguiente() = _mes.update { it.plusMonths(1) }

    fun guardar(movimiento: Movimiento) = viewModelScope.launch {
        dao.guardar(movimiento.copy(usuarioId = usuarioId.value))
    }
    fun eliminar(movimiento: Movimiento) = viewModelScope.launch { dao.eliminar(movimiento) }
}
