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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misfinanzas.app.data.TipoMovimiento
import com.misfinanzas.app.ui.theme.colorDe

@Composable
fun PantallaResumen(estado: EstadoMes, historico: Resumen) {
    val r = estado.resumen
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Tarjeta principal: dinero disponible del mes
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Disponible este mes", style = MaterialTheme.typography.labelLarge)
                Text(
                    dinero(r.disponible),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (r.disponible < 0) colorDe(TipoMovimiento.GASTO) else Color.Unspecified,
                )
                if (r.ingresos > 0) {
                    val usado = ((r.gastos + r.ahorroNeto) / r.ingresos).toFloat()
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { usado.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Has usado el ${(usado * 100).toInt()} % de tus ingresos " +
                            "(gastos + ahorro)",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Movimientos del mes", style = MaterialTheme.typography.titleMedium)
                FilaValor("Ingresos", r.ingresos, colorDe(TipoMovimiento.INGRESO))
                FilaValor("Gastos", r.gastos, colorDe(TipoMovimiento.GASTO))
                FilaValor("Ahorrado", r.ahorros, colorDe(TipoMovimiento.AHORRO))
                if (r.retiros > 0) FilaValor("Retirado de ahorros", r.retiros, colorDe(TipoMovimiento.RETIRO_AHORRO))
                if (r.ingresos > 0) {
                    HorizontalDivider()
                    val tasa = (r.ahorroNeto / r.ingresos * 100).toInt()
                    Text("Tasa de ahorro: $tasa % de tus ingresos", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (estado.gastosPorCategoria.isNotEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Gastos por categoría", style = MaterialTheme.typography.titleMedium)
                    val maximo = estado.gastosPorCategoria.first().second
                    val colorGasto = colorDe(TipoMovimiento.GASTO)
                    estado.gastosPorCategoria.forEach { (categoria, total) ->
                        Column {
                            Row(Modifier.fillMaxWidth()) {
                                Text(categoria, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                val pct = if (r.gastos > 0) (total / r.gastos * 100).toInt() else 0
                                Text("${dinero(total)}  ($pct %)", style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (total / maximo).toFloat() },
                                modifier = Modifier.fillMaxWidth(),
                                color = colorGasto,
                            )
                        }
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Balance general (todos los meses)", style = MaterialTheme.typography.titleMedium)
                FilaValor("Dinero disponible acumulado", historico.disponible, null)
                FilaValor("Total ahorrado", historico.ahorroNeto, colorDe(TipoMovimiento.AHORRO))
                HorizontalDivider()
                FilaValor("Patrimonio (disponible + ahorros)", historico.disponible + historico.ahorroNeto, null, negrita = true)
            }
        }

        if (estado.movimientos.isEmpty()) {
            Text(
                "Aún no hay movimientos en este mes. Toca «Agregar» para registrar tu ingreso, " +
                    "un gasto o un ahorro.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(88.dp)) // espacio para el botón flotante
    }
}

@Composable
private fun FilaValor(etiqueta: String, valor: Double, color: Color?, negrita: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(etiqueta, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(
            dinero(valor),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                valor < 0 -> colorDe(TipoMovimiento.GASTO)
                color != null -> color
                else -> Color.Unspecified
            },
        )
    }
}
