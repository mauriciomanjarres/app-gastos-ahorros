package com.misfinanzas.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.misfinanzas.app.data.Movimiento
import com.misfinanzas.app.data.TipoMovimiento
import java.time.LocalDate
import java.time.YearMonth

private const val MS_POR_DIA = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorMovimiento(
    existente: Movimiento?,
    tipoInicial: TipoMovimiento,
    mesActual: YearMonth,
    onGuardar: (Movimiento) -> Unit,
    onEliminar: (Movimiento) -> Unit,
    onCerrar: () -> Unit,
) {
    // Fecha por defecto: hoy si estamos viendo el mes actual, si no el primer día del mes visto.
    val fechaInicial = existente?.fecha ?: run {
        val hoy = LocalDate.now()
        if (YearMonth.from(hoy) == mesActual) hoy.toEpochDay() else mesActual.atDay(1).toEpochDay()
    }

    var tipo by remember { mutableStateOf(existente?.tipo ?: tipoInicial) }
    var montoTexto by remember {
        mutableStateOf(existente?.monto?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "")
    }
    var categoria by remember { mutableStateOf(existente?.categoria ?: tipo.categorias.first()) }
    var descripcion by remember { mutableStateOf(existente?.descripcion ?: "") }
    var fecha by remember { mutableStateOf(fechaInicial) }
    var mostrarCalendario by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf(false) }

    val monto = parsearMonto(montoTexto)
    val montoValido = monto != null && monto > 0

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (existente == null) "Nuevo movimiento" else "Editar movimiento") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Tipo", style = MaterialTheme.typography.labelLarge)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TipoMovimiento.entries.forEach { t ->
                        FilterChip(
                            selected = tipo == t,
                            onClick = {
                                tipo = t
                                if (categoria !in t.categorias) categoria = t.categorias.first()
                            },
                            label = { Text(t.etiqueta) },
                        )
                    }
                }

                when (tipo) {
                    TipoMovimiento.AHORRO -> "Se descuenta de tu dinero del mes. Si es dinero que ya tenías ahorrado, elige «Ahorro que ya tenía»."
                    TipoMovimiento.AHORRO_EXISTENTE -> "Suma a tus ahorros sin descontarse de tu sueldo ni de tu dinero del mes."
                    TipoMovimiento.RETIRO_AHORRO -> "Sacas dinero de tus ahorros y vuelve a estar disponible para gastar."
                    else -> null
                }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { nuevo -> montoTexto = nuevo.filter { it.isDigit() || it == '.' || it == ',' } },
                    label = { Text("Monto") },
                    prefix = { Text("$ ") },
                    singleLine = true,
                    isError = montoTexto.isNotEmpty() && !montoValido,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Categoría", style = MaterialTheme.typography.labelLarge)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    tipo.categorias.forEach { c ->
                        FilterChip(selected = categoria == c, onClick = { categoria = c }, label = { Text(c) })
                    }
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedButton(onClick = { mostrarCalendario = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Text("  ${fechaTexto(fecha)}")
                }

                if (existente != null) {
                    TextButton(onClick = { confirmarEliminar = true }) {
                        Text("Eliminar movimiento", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = montoValido,
                onClick = {
                    onGuardar(
                        Movimiento(
                            id = existente?.id ?: 0,
                            tipo = tipo,
                            monto = monto ?: 0.0,
                            categoria = categoria,
                            descripcion = descripcion.trim(),
                            fecha = fecha,
                        )
                    )
                },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } },
    )

    if (mostrarCalendario) {
        val estadoFecha = rememberDatePickerState(initialSelectedDateMillis = fecha * MS_POR_DIA)
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let { fecha = Math.floorDiv(it, MS_POR_DIA) }
                    mostrarCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = estadoFecha)
        }
    }

    if (confirmarEliminar && existente != null) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("¿Eliminar movimiento?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { onEliminar(existente) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } },
        )
    }
}
