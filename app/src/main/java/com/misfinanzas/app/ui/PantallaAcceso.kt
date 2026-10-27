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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misfinanzas.app.ui.theme.LogoM

private enum class Modo { INGRESAR, REGISTRARSE, RECUPERAR }

@Composable
fun PantallaAcceso(vm: SesionViewModel) {
    val acceso by vm.acceso.collectAsStateWithLifecycle()

    var modo by rememberSaveable { mutableStateOf(Modo.INGRESAR) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }

    fun cambiarModo(nuevo: Modo) {
        modo = nuevo
        contrasena = ""
        confirmacion = ""
        vm.limpiarMensajes()
    }

    val enviar: () -> Unit = {
        when (modo) {
            Modo.INGRESAR -> vm.iniciarSesion(correo, contrasena)
            Modo.REGISTRARSE -> vm.registrar(nombre, correo, contrasena, confirmacion)
            Modo.RECUPERAR -> vm.recuperarContrasena(correo)
        }
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
            LogoM(88.dp)
            Text(
                "Mis Finanzas",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                when (modo) {
                    Modo.INGRESAR -> "Ingresa con tu correo para ver tus finanzas."
                    Modo.REGISTRARSE -> "Crea tu cuenta con tu correo. Si olvidas la contraseña, podrás recuperarla desde ahí."
                    Modo.RECUPERAR -> "Escribe tu correo y te enviaremos un enlace para crear una contraseña nueva."
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            val ancho = Modifier.widthIn(max = 480.dp).fillMaxWidth()
            val habilitado = !acceso.cargando

            if (modo == Modo.REGISTRARSE) {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it; vm.limpiarMensajes() },
                    label = { Text("Tu nombre") }, singleLine = true, modifier = ancho, enabled = habilitado,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
            }
            OutlinedTextField(
                value = correo, onValueChange = { correo = it.trim(); vm.limpiarMensajes() },
                label = { Text("Correo electrónico") }, singleLine = true, modifier = ancho, enabled = habilitado,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = if (modo == Modo.RECUPERAR) ImeAction.Done else ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onDone = { enviar() }),
            )
            if (modo != Modo.RECUPERAR) {
                CampoContrasena(
                    valor = contrasena, onCambio = { contrasena = it; vm.limpiarMensajes() },
                    etiqueta = "Contraseña", modifier = ancho, habilitado = habilitado,
                    ultimo = modo == Modo.INGRESAR, onListo = enviar,
                )
            }
            if (modo == Modo.REGISTRARSE) {
                CampoContrasena(
                    valor = confirmacion, onCambio = { confirmacion = it; vm.limpiarMensajes() },
                    etiqueta = "Repite la contraseña", modifier = ancho, habilitado = habilitado,
                    ultimo = true, onListo = enviar,
                )
            }

            acceso.error?.let { Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
            acceso.aviso?.let { Text(it, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center) }

            Button(onClick = enviar, enabled = habilitado, modifier = ancho.height(52.dp)) {
                if (acceso.cargando) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        when (modo) {
                            Modo.INGRESAR -> "Ingresar"
                            Modo.REGISTRARSE -> "Crear cuenta"
                            Modo.RECUPERAR -> "Enviar enlace"
                        }
                    )
                }
            }

            when (modo) {
                Modo.INGRESAR -> {
                    TextButton(onClick = { cambiarModo(Modo.RECUPERAR) }, enabled = habilitado) {
                        Text("¿Olvidaste tu contraseña?")
                    }
                    TextButton(onClick = { cambiarModo(Modo.REGISTRARSE) }, enabled = habilitado) {
                        Text("¿No tienes cuenta? Regístrate")
                    }
                }
                Modo.REGISTRARSE, Modo.RECUPERAR -> {
                    TextButton(onClick = { cambiarModo(Modo.INGRESAR) }, enabled = habilitado) {
                        Text("Ya tengo cuenta: ingresar")
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoContrasena(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier,
    habilitado: Boolean,
    ultimo: Boolean,
    onListo: () -> Unit,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        enabled = habilitado,
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
