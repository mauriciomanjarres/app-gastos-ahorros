package com.misfinanzas.app.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misfinanzas.app.R
import com.misfinanzas.app.data.TipoMovimiento

// Paleta de marca: azul marino + verde esmeralda.
val Marino = Color(0xFF163A5F)
val Esmeralda = Color(0xFF12805C)

private val Claro = lightColorScheme(
    primary = Marino,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4F5),
    onPrimaryContainer = Color(0xFF0B1F33),
    secondary = Esmeralda,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3F2E4),
    onSecondaryContainer = Color(0xFF04291C),
    tertiary = Color(0xFFB7791F),
    background = Color(0xFFF3F5F9),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFF3F5F9),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE6EBF2),
    onSurfaceVariant = Color(0xFF5B6575),
    outline = Color(0xFFC2CAD6),
    outlineVariant = Color(0xFFE1E6ED),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    error = Color(0xFFC62828),
)

private val Oscuro = darkColorScheme(
    primary = Color(0xFF9CC3EE),
    onPrimary = Color(0xFF0B1F33),
    primaryContainer = Color(0xFF1E4A75),
    onPrimaryContainer = Color(0xFFD6E4F5),
    secondary = Color(0xFF6FD3A8),
    onSecondary = Color(0xFF04291C),
    secondaryContainer = Color(0xFF155C43),
    onSecondaryContainer = Color(0xFFD3F2E4),
    tertiary = Color(0xFFF0C26B),
    background = Color(0xFF0E141B),
    onBackground = Color(0xFFE5E9F0),
    surface = Color(0xFF0E141B),
    onSurface = Color(0xFFE5E9F0),
    surfaceVariant = Color(0xFF242E3A),
    onSurfaceVariant = Color(0xFFA9B3C1),
    outline = Color(0xFF3A4656),
    outlineVariant = Color(0xFF2A3440),
    surfaceContainerLowest = Color(0xFF0A0F14),
    surfaceContainerLow = Color(0xFF151C24),
    surfaceContainer = Color(0xFF161E27),
    surfaceContainerHigh = Color(0xFF1B2430),
    surfaceContainerHighest = Color(0xFF1B2430),
    error = Color(0xFFF28B82),
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val Tipografia = Typography().let { t ->
    t.copy(
        displaySmall = t.displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineLarge = t.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun TemaFinanzas(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        shapes = Formas,
        typography = Tipografia,
        content = content,
    )
}

@Composable
fun colorDe(tipo: TipoMovimiento): Color {
    val oscuro = isSystemInDarkTheme()
    return when (tipo) {
        TipoMovimiento.INGRESO -> if (oscuro) Color(0xFF6FD3A8) else Esmeralda
        TipoMovimiento.GASTO -> if (oscuro) Color(0xFFF28B82) else Color(0xFFC62828)
        TipoMovimiento.AHORRO, TipoMovimiento.AHORRO_EXISTENTE -> if (oscuro) Color(0xFF8AB4F8) else Color(0xFF1E5AA8)
        TipoMovimiento.RETIRO_AHORRO -> if (oscuro) Color(0xFFF0C26B) else Color(0xFFB7791F)
    }
}

/** Degradado de marca para las tarjetas destacadas. */
val DegradadoMarca = Brush.linearGradient(listOf(Marino, Color(0xFF1B5E7A), Esmeralda))

/** Tarjeta principal con fondo degradado y texto blanco. */
@Composable
fun TarjetaDestacada(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(DegradadoMarca)
            .padding(20.dp),
        content = content,
    )
}

/** Logo de la app: la letra M sobre un círculo con el degradado de marca. */
@Composable
fun LogoM(tamano: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(tamano)
            .clip(CircleShape)
            .background(DegradadoMarca),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Mis Finanzas",
            modifier = Modifier.fillMaxSize(),
        )
    }
}
