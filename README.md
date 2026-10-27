# Mis Finanzas

App Android para llevar tus **ingresos, gastos y ahorros** mes a mes y ver tu **balance general**.
Todo se guarda en el teléfono (base de datos local, sin internet ni cuentas).

## Qué hace

- **Cuenta con contraseña**: la primera vez creas tu usuario y contraseña; después la app pide
  iniciar sesión. La contraseña se guarda cifrada (hash PBKDF2) solo en el teléfono.
- **Registrar movimientos** de cuatro tipos: Ingreso, Gasto, Ahorro y Retiro de ahorro, con monto,
  categoría, descripción y fecha. Toca un movimiento para editarlo o eliminarlo.
- **Resumen del mes**: ingresos, gastos, lo ahorrado, dinero disponible
  (`ingresos − gastos − ahorro + retiros`), porcentaje de ingresos usado y tasa de ahorro.
- **Gastos por categoría** con barras para ver en qué se va el dinero.
- **Balance general** de todos los meses: dinero disponible acumulado, total ahorrado y patrimonio.
- Navegación entre meses con las flechas.

## Cómo obtener el APK

**Descarga directa:** cada cambio en `main` se compila solo (`.github/workflows/build-apk.yml`)
y el APK queda publicado en *Releases* → **Mis Finanzas (última versión)** → `MisFinanzas.apk`.
Todas las versiones usan la misma firma (`app/firma-app.jks`), así que puedes instalar una nueva
encima de la anterior sin perder tus datos.

**Con Android Studio:** abre la carpeta del proyecto, espera a que sincronice y pulsa *Run*,
o usa *Build → Build APK(s)*. Desde terminal: `./gradlew assembleDebug`
(el APK queda en `app/build/outputs/apk/debug/app-debug.apk`).

Para instalar el APK en el teléfono, permite «Instalar apps desconocidas» para el navegador o
el gestor de archivos con el que lo abras.

## Tecnología

Kotlin · Jetpack Compose (Material 3) · Room · ViewModel + Flow. minSdk 26 (Android 8.0).
