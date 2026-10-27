package com.misfinanzas.app.ui

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.misfinanzas.app.data.Plan
import com.misfinanzas.app.data.TipoMovimiento
import com.misfinanzas.app.data.TotalPorCategoria
import com.misfinanzas.app.ui.theme.colorDe
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun PantallaAhorros(
    plan: Plan,
    estado: EstadoMes,
    historico: Resumen,
    porCategoria: List<TotalPorCategoria>,
    onGuardarPlan: (Plan) -> Unit,
    onAgregar: (TipoMovimiento) -> Unit,
) {
    var editarMeta by remember { mutableStateOf(false) }
    val total = historico.ahorroNeto
    val colorAhorro = colorDe(TipoMovimiento.AHORRO)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Total ahorrado", style = MaterialTheme.typography.labelLarge)
                Text(dinero(total), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                if (plan.ahorroMensual > 0) {
                    Text(
                        "Tu plan: ahorrar ${dinero(plan.ahorroMensual)} cada mes",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onAgregar(TipoMovimiento.AHORRO) }, modifier = Modifier.weight(1f)) {
                Text("Agregar ahorro")
            }
            OutlinedButton(onClick = { onAgregar(TipoMovimiento.RETIRO_AHORRO) }, modifier = Modifier.weight(1f)) {
                Text("Retirar")
            }
        }
        OutlinedButton(onClick = { onAgregar(TipoMovimiento.AHORRO_EXISTENTE) }, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar ahorro que ya tenía")
        }
        Text(
            "«Agregar ahorro» sale de tu sueldo del mes. «Ahorro que ya tenía» es dinero que ahorraste " +
                "antes: suma a tu total pero no se descuenta de tu sueldo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row {
                    Text("Mi meta de ahorro", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { editarMeta = true }) {
                        Text(if (plan.montoMeta > 0) "Editar" else "Definir")
                    }
                }
                if (plan.montoMeta > 0) {
                    val avance = (total / plan.montoMeta).coerceIn(0.0, 1.0)
                    Text(plan.nombreMeta.ifBlank { "Meta" }, style = MaterialTheme.typography.bodyLarge)
                    LinearProgressIndicator(
                        progress = { avance.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = colorAhorro,
                    )
                    Fila("${(avance * 100).roundToInt()} % de ${dinero(plan.montoMeta)}", dinero(total), colorAhorro)
                    val falta = plan.montoMeta - total
                    if (falta <= 0) {
                        Text("¡Lograste tu meta! 🎉", fontWeight = FontWeight.Bold, color = colorAhorro)
                    } else {
                        Fila("Te faltan", dinero(falta), null, negrita = true)
                        if (plan.ahorroMensual > 0) {
                            val meses = ceil(falta / plan.ahorroMensual).toInt()
                            Text(
                                "Con tu plan la alcanzas en $meses ${if (meses == 1) "mes" else "meses"}.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                } else {
                    Text(
                        "Ponte una meta (por ejemplo, un viaje o un fondo de emergencia) y mira cuánto te falta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Este mes", style = MaterialTheme.typography.titleMedium)
                Fila("Ahorrado", dinero(estado.resumen.ahorros), colorAhorro)
                Fila("Retirado", dinero(estado.resumen.retiros), colorDe(TipoMovimiento.RETIRO_AHORRO))
                if (estado.resumen.existentes > 0) {
                    Fila("Ahorro que ya tenías", dinero(estado.resumen.existentes), colorAhorro)
                }
            }
        }

        if (porCategoria.isNotEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dónde está tu ahorro", style = MaterialTheme.typography.titleMedium)
                    porCategoria.forEachIndexed { i, c ->
                        if (i > 0) HorizontalDivider()
                        Fila(c.categoria, dinero(c.total), null)
                    }
                }
            }
        }
        Spacer(Modifier.height(88.dp))
    }

    if (editarMeta) {
        var nombre by remember { mutableStateOf(plan.nombreMeta) }
        var montoTexto by remember { mutableStateOf(numeroEditable(plan.montoMeta)) }
        AlertDialog(
            onDismissRequest = { editarMeta = false },
            title = { Text("Meta de ahorro") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nombre, onValueChange = { nombre = it },
                        label = { Text("¿Para qué ahorras?") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = montoTexto, onValueChange = { montoTexto = soloNumero(it) },
                        label = { Text("Monto de la meta") }, prefix = { Text("$ ") }, singleLine = true,
                        supportingText = { parsearMonto(montoTexto)?.let { Text(dinero(it)) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onGuardarPlan(plan.copy(nombreMeta = nombre.trim(), montoMeta = parsearMonto(montoTexto) ?: 0.0))
                    editarMeta = false
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editarMeta = false }) { Text("Cancelar") } },
        )
    }
}
