package com.misfinanzas.app.ui

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import java.time.LocalDate
import java.time.YearMonth

private const val MS_DIA = 86_400_000L

/** Calendario para elegir una fecha (epochDay). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoFecha(fecha: Long, onElegir: (Long) -> Unit, onCerrar: () -> Unit) {
    val estado = rememberDatePickerState(initialSelectedDateMillis = fecha * MS_DIA)
    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(onClick = {
                estado.selectedDateMillis?.let { onElegir(Math.floorDiv(it, MS_DIA)) }
                onCerrar()
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } },
    ) {
        DatePicker(state = estado)
    }
}

/**
 * Fecha de pago sugerida para el mes visto: hoy si es el mes actual; si no, el mismo día del
 * último pago (ajustado al largo del mes) o el día 1.
 */
fun fechaPagoSugerida(ultimoPago: Long?, mes: YearMonth): Long {
    val hoy = LocalDate.now()
    if (YearMonth.from(hoy) == mes) return hoy.toEpochDay()
    val dia = ultimoPago?.let { LocalDate.ofEpochDay(it).dayOfMonth } ?: 1
    return mes.atDay(minOf(dia, mes.lengthOfMonth())).toEpochDay()
}
