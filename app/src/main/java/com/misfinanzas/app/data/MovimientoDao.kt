package com.misfinanzas.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {
    @Query("SELECT * FROM movimientos WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC, id DESC")
    fun delPeriodo(desde: Long, hasta: Long): Flow<List<Movimiento>>

    @Query("SELECT tipo, SUM(monto) AS total FROM movimientos GROUP BY tipo")
    fun totalesHistoricos(): Flow<List<TotalPorTipo>>

    @Upsert
    suspend fun guardar(movimiento: Movimiento)

    @Delete
    suspend fun eliminar(movimiento: Movimiento)
}
