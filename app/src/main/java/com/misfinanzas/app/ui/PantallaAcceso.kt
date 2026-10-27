package com.misfinanzas.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PantallaAcceso(vm: SesionViewModel, registro: Boolean) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var usuario by rememberSaveable { mutableStateOf(if (registro) "" else vm.usuario) }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmarReinicio by rememberSaveable { mutableStateOf(false) }

    val enviar = {
        error = if (registro) vm.registrar(nombre, usuario, contrasena, confirmacion)
        else vm.iniciarSesion(usuario, contrasena)
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                "Mis Finanzas",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                if (registro) "Crea tu cuenta para proteger tu información. Todo se guarda solo en este teléfono."
                else "Hola de nuevo, ${vm.nombre}. Ingresa para ver tus finanzas.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            val ancho = Modifier.widthIn(max = 480.dp).fillMaxWidth()
            if (registro) {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it; error = null },
                    label = { Text("Tu nombre") }, singleLine = true, modifier = ancho,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
            }
            OutlinedTextField(
                value = usuario, onValueChange = { usuario = it; error = null },
                label = { Text("Usuario") }, singleLine = true, modifier = ancho,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )
            CampoContrasena(
                valor = contrasena, onCambio = { contrasena = it; error = null },
                etiqueta = "Contraseña", modifier = ancho,
                ultimo = !registro, onListo = enviar,
            )
            if (registro) {
                CampoContrasena(
                    valor = confirmacion, onCambio = { confirmacion = it; error = null },
                    etiqueta = "Repite la contraseña", modifier = ancho,
                    ultimo = true, onListo = enviar,
                )
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = enviar, modifier = ancho.height(52.dp)) {
                Text(if (registro) "Crear cuenta" else "Ingresar")
            }

            if (!registro) {
                TextButton(onClick = { confirmarReinicio = true }) { Text("¿Olvidaste tu contraseña?") }
            }
        }
    }

    if (confirmarReinicio) {
        AlertDialog(
            onDismissRequest = { confirmarReinicio = false },
            title = { Text("¿Empezar de cero?") },
            text = {
                Text(
                    "Por seguridad la contraseña no se puede recuperar. Puedes borrar la cuenta y " +
                        "todos los movimientos guardados para crear una cuenta nueva."
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmarReinicio = false; vm.reiniciarTodo() }) {
                    Text("Borrar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmarReinicio = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun CampoContrasena(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier,
    ultimo: Boolean,
    onListo: () -> Unit,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        modifier = modifier,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Ver") }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (ultimo) ImeAction.Done else ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onDone = { onListo() }),
    )
}
