package com.misfinanzas.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.misfinanzas.app.data.Movimiento
import java.time.YearMonth

/** Estado del editor: null = cerrado; Nuevo = crear; Editar = modificar uno existente. */
sealed interface Editor {
    data object Nuevo : Editor
    data class Editar(val movimiento: Movimiento) : Editor
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFinanzas(
    uid: String,
    nombre: String,
    onCerrarSesion: () -> Unit,
    vm: FinanzasViewModel = viewModel(),
) {
    LaunchedEffect(uid) { vm.usarCuenta(uid) }
    val mes by vm.mes.collectAsStateWithLifecycle()
    val estado by vm.estadoMes.collectAsStateWithLifecycle()
    val historico by vm.historico.collectAsStateWithLifecycle()

    var pestana by rememberSaveable { mutableStateOf(0) }
    var editor by remember { mutableStateOf<Editor?>(null) }
    var confirmarSalida by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    fun avisar(texto: String) {
        alcance.launch { snackbar.showSnackbar(texto, duration = SnackbarDuration.Short) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis Finanzas", fontWeight = FontWeight.Bold)
                        Text(
                            "Hola, ${nombre.substringBefore(' ')}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { confirmarSalida = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestana == 0,
                    onClick = { pestana = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Resumen") },
                )
                NavigationBarItem(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Movimientos") },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editor = Editor.Nuevo },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Agregar") },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SelectorMes(mes, onAnterior = vm::mesAnterior, onSiguiente = vm::mesSiguiente)
            when (pestana) {
                0 -> PantallaResumen(estado, historico)
                else -> PantallaMovimientos(estado.movimientos, onSeleccionar = { editor = Editor.Editar(it) })
            }
        }
    }

    editor?.let { e ->
        EditorMovimiento(
            existente = (e as? Editor.Editar)?.movimiento,
            mesActual = mes,
            onGuardar = {
                vm.guardar(it)
                avisar(if (e is Editor.Editar) "Cambios guardados" else "${it.tipo.etiqueta} registrado")
                editor = null
            },
            onEliminar = { vm.eliminar(it); avisar("Movimiento eliminado"); editor = null },
            onCerrar = { editor = null },
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus datos quedan guardados. Necesitarás tu contraseña para volver a entrar.") },
            confirmButton = { TextButton(onClick = { confirmarSalida = false; onCerrarSesion() }) { Text("Cerrar sesión") } },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun SelectorMes(mes: YearMonth, onAnterior: () -> Unit, onSiguiente: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onAnterior) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
        }
        Text(
            mes.texto(),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onSiguiente) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
        }
    }
}
