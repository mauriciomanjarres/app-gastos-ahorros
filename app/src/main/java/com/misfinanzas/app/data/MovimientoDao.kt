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

    @Upsert
    suspend fun guardar(movimiento: Movimiento)

    @Delete
    suspend fun eliminar(movimiento: Movimiento)
}
