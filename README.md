# Mis Finanzas

App Android para llevar tus **ingresos, gastos y ahorros** mes a mes y ver tu **balance general**.
Los movimientos se guardan en el teléfono; el inicio de sesión usa Firebase (requiere internet para entrar).

## Qué hace

- **Cuenta con correo (Firebase Authentication)**: registro e inicio de sesión con correo y contraseña,
  y «¿Olvidaste tu contraseña?» envía un enlace de recuperación al correo. Los movimientos se guardan
  en el teléfono, separados por cuenta.
- **Sueldo**: escribes tu sueldo mensual, eliges cuánto ahorrar (porcentaje o monto fijo) y ves
  cuánto te queda para gastar y cuánto puedes gastar todavía según tus gastos del mes. Con un toque
  registras el sueldo y el ahorro del mes.
- **Ahorros**: total ahorrado, meta de ahorro con progreso y meses estimados para lograrla,
  y botones para agregar o retirar ahorro.
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

Kotlin · Jetpack Compose (Material 3) · Room · Firebase Auth · ViewModel + Flow. minSdk 26 (Android 8.0).
