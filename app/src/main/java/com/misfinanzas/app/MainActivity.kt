package com.misfinanzas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.misfinanzas.app.ui.AppFinanzas
import com.misfinanzas.app.ui.PantallaAcceso
import com.misfinanzas.app.ui.SesionViewModel
import com.misfinanzas.app.ui.theme.TemaFinanzas

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemaFinanzas {
                val sesion: SesionViewModel = viewModel()
                val usuario by sesion.usuario.collectAsStateWithLifecycle()
                val actual = usuario
                if (actual == null) {
                    PantallaAcceso(sesion)
                } else {
                    AppFinanzas(
                        uid = actual.uid,
                        nombre = actual.displayName?.takeIf { it.isNotBlank() }
                            ?: actual.email?.substringBefore('@').orEmpty(),
                        onCerrarSesion = sesion::cerrarSesion,
                    )
                }
            }
        }
    }
}
