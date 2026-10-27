package com.misfinanzas.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoMovimiento(val etiqueta: String) {
    INGRESO("Ingreso"),
    GASTO("Gasto"),
    AHORRO("Ahorro"),
    RETIRO_AHORRO("Retiro de ahorro");

    val categorias: List<String>
        get() = when (this) {
            INGRESO -> listOf("Salario", "Negocio", "Trabajo extra", "Regalo", "Otros")
            GASTO -> listOf(
                "Alimentación", "Vivienda", "Servicios", "Transporte", "Salud",
                "Educación", "Entretenimiento", "Ropa", "Deudas", "Otros"
            )
            AHORRO, RETIRO_AHORRO -> listOf("Fondo de emergencia", "Meta personal", "Inversión", "Otros")
        }
}

@Entity(tableName = "movimientos")
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: TipoMovimiento,
    val monto: Double,
    val categoria: String,
    val descripcion: String = "",
    /** Fecha como LocalDate.toEpochDay(). */
    val fecha: Long,
    /** uid de Firebase del dueño del movimiento. */
    @ColumnInfo(defaultValue = "")
    val usuarioId: String = "",
)

data class TotalPorTipo(
    val tipo: TipoMovimiento,
    val total: Double,
)

data class TotalPorCategoria(
    val categoria: String,
    val total: Double,
)
