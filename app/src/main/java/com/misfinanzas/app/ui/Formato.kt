package com.misfinanzas.app.ui

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeEs = Locale("es", "CO")

private val formatoMoneda: NumberFormat = NumberFormat.getCurrencyInstance(localeEs).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 2
}

fun dinero(valor: Double): String = formatoMoneda.format(valor)

private val formatoMes = DateTimeFormatter.ofPattern("MMMM yyyy", localeEs)
private val formatoFecha = DateTimeFormatter.ofPattern("d MMM yyyy", localeEs)

fun YearMonth.texto(): String = format(formatoMes).replaceFirstChar { it.uppercase() }

fun fechaTexto(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(formatoFecha)

/** Acepta "1.500,50", "1500.5" o "1500". Devuelve null si no es un número válido. */
fun parsearMonto(texto: String): Double? {
    val limpio = texto.trim().replace(" ", "")
    if (limpio.isEmpty()) return null
    val normalizado = when {
        limpio.contains(',') -> limpio.replace(".", "").replace(',', '.')
        limpio.count { it == '.' } > 1 -> limpio.replace(".", "")
        // "1.500" con 3 dígitos tras el punto se interpreta como separador de miles
        limpio.contains('.') && limpio.substringAfter('.').length == 3 -> limpio.replace(".", "")
        else -> limpio
    }
    return normalizado.toDoubleOrNull()
}
