package com.misfinanzas.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.misfinanzas.app.data.Movimiento
import com.misfinanzas.app.data.TipoMovimiento
import com.misfinanzas.app.ui.theme.colorDe

@Composable
fun PantallaMovimientos(movimientos: List<Movimiento>, onSeleccionar: (Movimiento) -> Unit) {
    if (movimientos.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "No hay movimientos en este mes.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
        items(movimientos, key = { it.id }) { m ->
            val signo = when (m.tipo) {
                TipoMovimiento.INGRESO, TipoMovimiento.RETIRO_AHORRO -> "+"
                TipoMovimiento.GASTO, TipoMovimiento.AHORRO -> "−"
            }
            ListItem(
                modifier = Modifier.clickable { onSeleccionar(m) },
                overlineContent = { Text(m.tipo.etiqueta, color = colorDe(m.tipo)) },
                headlineContent = { Text(m.categoria) },
                supportingContent = {
                    Text(listOf(m.descripcion, fechaTexto(m.fecha)).filter { it.isNotBlank() }.joinToString(" · "))
                },
                trailingContent = {
                    Text(
                        "$signo${dinero(m.monto)}",
                        color = colorDe(m.tipo),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
            )
            HorizontalDivider()
        }
    }
}
