package com.misfinanzas.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.misfinanzas.app.data.TipoMovimiento

private val Verde = Color(0xFF1B7F5A)

private val Claro = lightColorScheme(primary = Verde, secondary = Color(0xFF3D6A8A))
private val Oscuro = darkColorScheme(primary = Color(0xFF7AD9AE), secondary = Color(0xFFA4CBEB))

@Composable
fun TemaFinanzas(content: @Composable () -> Unit) {
    val oscuro = isSystemInDarkTheme()
    val esquema = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (oscuro) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        oscuro -> Oscuro
        else -> Claro
    }
    MaterialTheme(colorScheme = esquema, content = content)
}

@Composable
fun colorDe(tipo: TipoMovimiento): Color {
    val oscuro = isSystemInDarkTheme()
    return when (tipo) {
        TipoMovimiento.INGRESO -> if (oscuro) Color(0xFF81C995) else Color(0xFF1E8E3E)
        TipoMovimiento.GASTO -> if (oscuro) Color(0xFFF28B82) else Color(0xFFC5221F)
        TipoMovimiento.AHORRO -> if (oscuro) Color(0xFF8AB4F8) else Color(0xFF1967D2)
        TipoMovimiento.RETIRO_AHORRO -> if (oscuro) Color(0xFFFDD663) else Color(0xFFB06000)
    }
}
