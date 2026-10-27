package com.misfinanzas.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.misfinanzas.app.data.ModoAhorro
import com.misfinanzas.app.data.Plan
import com.misfinanzas.app.data.TipoMovimiento
import com.misfinanzas.app.ui.theme.colorDe
import java.time.YearMonth
import kotlin.math.roundToInt

@Composable
fun PantallaSueldo(
    plan: Plan,
    mes: YearMonth,
    estado: EstadoMes,
    onGuardarPlan: (Plan) -> Unit,
    onRegistrarMes: (Plan) -> Unit,
) {
    // Se reinicia solo cuando el plan guardado cambia (al cargar o al guardar).
    var sueldoTexto by remember(plan) { mutableStateOf(numeroEditable(plan.sueldo)) }
    var modo by remember(plan) { mutableStateOf(plan.modoAhorro) }
    var porcentaje by remember(plan) {
        mutableStateOf(if (plan.modoAhorro == ModoAhorro.PORCENTAJE) plan.valorAhorro.toFloat() else 10f)
    }
    var montoTexto by remember(plan) {
        mutableStateOf(if (plan.modoAhorro == ModoAhorro.MONTO) numeroEditable(plan.valorAhorro) else "")
    }

    val sueldo = parsearMonto(sueldoTexto) ?: 0.0
    val borrador = plan.copy(
        sueldo = sueldo,
        modoAhorro = modo,
        valorAhorro = if (modo == ModoAhorro.PORCENTAJE) porcentaje.toDouble() else parsearMonto(montoTexto) ?: 0.0,
    )
    val montoExcede = modo == ModoAhorro.MONTO && (parsearMonto(montoTexto) ?: 0.0) > sueldo
    val yaRegistrado = estado.movimientos.any { it.descripcion == DESCRIPCION_SUELDO }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Tu sueldo mensual", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = sueldoTexto,
                    onValueChange = { sueldoTexto = soloNumero(it) },
                    label = { Text("Sueldo base") },
                    prefix = { Text("$ ") },
                    singleLine = true,
                    supportingText = { if (sueldo > 0) Text(dinero(sueldo)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("2. ¿Cuánto quieres ahorrar?", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = modo == ModoAhorro.PORCENTAJE,
                        onClick = { modo = ModoAhorro.PORCENTAJE },
                        label = { Text("Porcentaje") },
                    )
                    FilterChip(
                        selected = modo == ModoAhorro.MONTO,
                        onClick = { modo = ModoAhorro.MONTO },
                        label = { Text("Monto fijo") },
                    )
                }
                if (modo == ModoAhorro.PORCENTAJE) {
                    Text(
                        "${porcentaje.roundToInt()} % de tu sueldo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorDe(TipoMovimiento.AHORRO),
                    )
                    Slider(
                        value = porcentaje,
                        onValueChange = { porcentaje = it.roundToInt().toFloat() },
                        valueRange = 0f..100f,
                    )
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(5, 10, 15, 20, 25, 30, 50).forEach { p ->
                            FilterChip(
                                selected = porcentaje.roundToInt() == p,
                                onClick = { porcentaje = p.toFloat() },
                                label = { Text("$p %") },
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = montoTexto,
                        onValueChange = { montoTexto = soloNumero(it) },
                        label = { Text("Monto a ahorrar cada mes") },
                        prefix = { Text("$ ") },
                        singleLine = true,
                        isError = montoExcede,
                        supportingText = {
                            if (montoExcede) Text("Es más que tu sueldo")
                            else parsearMonto(montoTexto)?.let { Text(dinero(it)) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Resultado
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Te queda de tu sueldo", style = MaterialTheme.typography.labelLarge)
                Text(
                    dinero(borrador.quedaParaGastar),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                HorizontalDivider()
                Fila("Sueldo", dinero(sueldo), null)
                Fila(
                    "Ahorro (${borrador.porcentajeAhorro.roundToInt()} %)",
                    "−${dinero(borrador.ahorroMensual)}",
                    colorDe(TipoMovimiento.AHORRO),
                )
                if (sueldo > 0) {
                    LinearProgressIndicator(
                        progress = { (borrador.ahorroMensual / sueldo).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = colorDe(TipoMovimiento.AHORRO),
                    )
                }
                val gastos = estado.resumen.gastos
                if (gastos > 0 && sueldo > 0) {
                    HorizontalDivider()
                    val libre = borrador.quedaParaGastar - gastos
                    Fila("Gastos de ${mes.texto()}", "−${dinero(gastos)}", colorDe(TipoMovimiento.GASTO))
                    Fila(
                        if (libre >= 0) "Aún puedes gastar" else "Te pasaste por",
                        dinero(kotlin.math.abs(libre)),
                        if (libre >= 0) null else colorDe(TipoMovimiento.GASTO),
                        negrita = true,
                    )
                }
            }
        }

        Button(
            onClick = { onRegistrarMes(borrador) },
            enabled = sueldo > 0 && !montoExcede,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(if (yaRegistrado) "Actualizar sueldo y ahorro de ${mes.texto()}" else "Registrar sueldo y ahorro en ${mes.texto()}")
        }
        OutlinedButton(
            onClick = { onGuardarPlan(borrador) },
            enabled = !montoExcede,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Solo guardar mi plan") }
        Text(
            "Al registrar, el sueldo se suma como ingreso del mes y el ahorro pasa a tus ahorros, " +
                "así el resumen y el balance quedan al día. Tu plan se recuerda para los próximos meses.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun Fila(etiqueta: String, valor: String, color: Color?, negrita: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(etiqueta, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(
            valor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.SemiBold,
            color = color ?: Color.Unspecified,
        )
    }
}

internal fun soloNumero(texto: String) = texto.filter { it.isDigit() || it == '.' || it == ',' }

internal fun numeroEditable(valor: Double): String = when {
    valor <= 0.0 -> ""
    valor % 1.0 == 0.0 -> valor.toLong().toString()
    else -> valor.toString()
}
