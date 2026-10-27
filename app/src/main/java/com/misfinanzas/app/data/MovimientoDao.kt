package com.misfinanzas.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {
    @Query(
        "SELECT * FROM movimientos WHERE usuarioId = :usuarioId AND fecha BETWEEN :desde AND :hasta " +
            "ORDER BY fecha DESC, id DESC"
    )
    fun delPeriodo(usuarioId: String, desde: Long, hasta: Long): Flow<List<Movimiento>>

    @Query("SELECT tipo, SUM(monto) AS total FROM movimientos WHERE usuarioId = :usuarioId GROUP BY tipo")
    fun totalesHistoricos(usuarioId: String): Flow<List<TotalPorTipo>>

    /** Los movimientos creados antes de tener cuentas con correo pasan al primer usuario que entra. */
    @Query("UPDATE movimientos SET usuarioId = :usuarioId WHERE usuarioId = ''")
    suspend fun adoptarSinDueno(usuarioId: String)

    /** Ahorro neto (ahorros − retiros) agrupado por categoría, de todos los meses. */
    @Query(
        "SELECT categoria, SUM(CASE WHEN tipo = 'RETIRO_AHORRO' THEN -monto ELSE monto END) AS total " +
            "FROM movimientos WHERE usuarioId = :usuarioId AND tipo IN ('AHORRO', 'AHORRO_EXISTENTE', 'RETIRO_AHORRO') " +
            "GROUP BY categoria ORDER BY total DESC"
    )
    fun ahorrosPorCategoria(usuarioId: String): Flow<List<TotalPorCategoria>>

    @Query(
        "SELECT * FROM movimientos WHERE usuarioId = :usuarioId AND descripcion = :descripcion " +
            "AND fecha BETWEEN :desde AND :hasta ORDER BY id"
    )
    suspend fun buscarPorDescripcion(usuarioId: String, descripcion: String, desde: Long, hasta: Long): List<Movimiento>

    @Upsert
    suspend fun guardar(movimiento: Movimiento)

    @Delete
    suspend fun eliminar(movimiento: Movimiento)
}
