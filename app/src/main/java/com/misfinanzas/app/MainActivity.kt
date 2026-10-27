package com.misfinanzas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.misfinanzas.app.ui.AppFinanzas
import com.misfinanzas.app.ui.EstadoSesion
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
                val estado by sesion.estado.collectAsStateWithLifecycle()
                when (estado) {
                    EstadoSesion.REGISTRO -> PantallaAcceso(sesion, registro = true)
                    EstadoSesion.LOGIN -> PantallaAcceso(sesion, registro = false)
                    EstadoSesion.DENTRO -> AppFinanzas(
                        nombre = sesion.nombre,
                        onCerrarSesion = sesion::cerrarSesion,
                    )
                }
            }
        }
    }
}
