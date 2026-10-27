package com.misfinanzas.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
                Text("Dinero libre este mes", style = MaterialTheme.typography.labelLarge)
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
                Text("Cómo se calcula tu mes", style = MaterialTheme.typography.titleMedium)
                LineaCalculo("Lo que ganaste", "Sueldo y otros ingresos", "+", r.ingresos, colorDe(TipoMovimiento.INGRESO))
                LineaCalculo("Lo que gastaste", "Todos tus gastos del mes", "−", r.gastos, colorDe(TipoMovimiento.GASTO))
                LineaCalculo("Lo que guardaste", "Dinero que pasaste a tus ahorros", "−", r.ahorros, colorDe(TipoMovimiento.AHORRO))
                if (r.retiros > 0) {
                    LineaCalculo("Lo que sacaste de ahorros", "Vuelve a estar disponible", "+", r.retiros, colorDe(TipoMovimiento.RETIRO_AHORRO))
                }
                HorizontalDivider()
                LineaCalculo("Te queda disponible", "Dinero libre de este mes", "=", r.disponible, null, total = true)
                if (r.ingresos > 0) {
                    val tasa = (r.ahorroNeto / r.ingresos * 100).toInt()
                    Text(
                        "Este mes ahorraste el $tasa % de lo que ganaste.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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

        BalanceGeneral(historico)

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

/**
 * Balance de todos los meses explicado paso a paso:
 * primero cuánto dinero libre tienes, luego tus ahorros, y al final el total de tu dinero.
 */
@Composable
private fun BalanceGeneral(h: Resumen) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Balance general", style = MaterialTheme.typography.titleMedium)
            Text(
                "Suma de todos los meses desde que empezaste a usar la app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (h.ingresos == 0.0 && h.gastos == 0.0 && h.ahorros == 0.0) {
                Text("Todavía no hay datos. Registra tu sueldo o un gasto para ver tu balance.")
                return@Column
            }

            Subtitulo("1. Tu dinero libre")
            LineaCalculo("Todo lo que has ganado", null, "+", h.ingresos, colorDe(TipoMovimiento.INGRESO))
            LineaCalculo("Todo lo que has gastado", null, "−", h.gastos, colorDe(TipoMovimiento.GASTO))
            LineaCalculo("Lo que pasaste a ahorros", null, "−", h.ahorros, colorDe(TipoMovimiento.AHORRO))
            if (h.retiros > 0) {
                LineaCalculo("Lo que sacaste de ahorros", null, "+", h.retiros, colorDe(TipoMovimiento.RETIRO_AHORRO))
            }
            HorizontalDivider()
            LineaCalculo(
                "Dinero libre", "Lo que deberías tener en tu cuenta o bolsillo para gastar",
                "=", h.disponible, null, total = true,
            )

            Spacer(Modifier.height(4.dp))
            Subtitulo("2. Tus ahorros")
            LineaCalculo(
                "Ahorros guardados", "Lo que has ahorrado menos lo que has retirado",
                "", h.ahorroNeto, colorDe(TipoMovimiento.AHORRO), total = true,
            )

            Spacer(Modifier.height(4.dp))
            Subtitulo("3. Todo tu dinero")
            LineaCalculo(
                "Dinero libre + ahorros", "Lo que tienes en total (ganado − gastado)",
                "=", h.disponible + h.ahorroNeto, null, total = true,
            )

            Spacer(Modifier.height(4.dp))
            val (mensaje, color) = when {
                h.gastos > h.ingresos ->
                    "Cuidado: has gastado más de lo que has ganado." to colorDe(TipoMovimiento.GASTO)
                h.disponible < 0 ->
                    "Has apartado para ahorro más de lo que te quedaba libre. Revisa tus gastos." to
                        colorDe(TipoMovimiento.RETIRO_AHORRO)
                else -> {
                    val pct = if (h.ingresos > 0) (h.gastos / h.ingresos * 100).toInt() else 0
                    "Vas bien: has gastado el $pct % de lo que has ganado." to colorDe(TipoMovimiento.INGRESO)
                }
            }
            Text(mensaje, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
private fun Subtitulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

/** Una línea de un cálculo: signo, concepto (con explicación opcional) y valor. */
@Composable
private fun LineaCalculo(
    concepto: String,
    explicacion: String?,
    signo: String,
    valor: Double,
    color: Color?,
    total: Boolean = false,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            signo,
            modifier = Modifier.width(20.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(Modifier.weight(1f)) {
            Text(
                concepto,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (total) FontWeight.Bold else FontWeight.Normal,
            )
            if (explicacion != null) {
                Text(
                    explicacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            dinero(valor),
            style = if (total) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (total) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                valor < 0 -> colorDe(TipoMovimiento.GASTO)
                color != null -> color
                else -> Color.Unspecified
            },
        )
    }
}
