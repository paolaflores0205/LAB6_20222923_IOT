# Laboratorio 6 - Sesiones de estudio

Abre `Solucion_Lab6.html` en un navegador: contiene la explicación paso a paso, el código completo y la guía de pruebas.

## Implementación

- FirebaseUI: correo/contraseña y Google, registro y recuperación.
- Cloud Firestore: CRUD en `usuarios/{uid}/sesiones/{id}` y lista en tiempo real.
- Formulario único para registrar y editar; horas decimales.
- Estadísticas: barras por mes/año y torta por actividad; filtro común por curso.
- Reglas de seguridad en `firestore.rules` para impedir acceso entre cuentas.

## Configuración pendiente de verificar en tu consola

1. Habilitar correo/contraseña y Google en Firebase Authentication.
2. Registrar SHA-1 y SHA-256 del equipo (`gradlew.bat :app:signingReport`) y actualizar `app/google-services.json` cuando corresponda.
3. Crear Cloud Firestore y publicar el contenido de `firestore.rules`.
4. Probar el flujo completo con dos cuentas reales.
5. Compartir el proyecto Firebase con la docente y entregar el repositorio como indica el enunciado.

No se publicaron reglas, repositorios ni invitaciones desde esta tarea.

## Entorno conservado

AGP 9.1.1, Gradle 9.3.1, compileSdk 36.1 y targetSdk 36. El daemon de Gradle solicita Java 21. Panda 1 no admite AGP 9.1; verifica la edición exacta de la universidad antes de cambiar versiones.

## Verificación

`gradlew.bat :app:assembleDebug :app:lintDebug :app:testDebugUnitTest`

Resultado: compilación correcta, lint sin errores (con advertencias), 6 pruebas aprobadas (5 pruebas nuevas de cálculos y 1 del proyecto inicial).
APK: `app/build/outputs/apk/debug/app-debug.apk`.
La apertura inicial y el selector personalizado de correo y Google también se comprobaron en un emulador; el acceso real y las operaciones con la nube requieren verificar la configuración de Firebase.

## Ajuste del teclado

El formulario usa adjustResize y reserva el espacio del teclado sin duplicar márgenes. La navegación inferior se oculta al escribir y reaparece al cerrar el teclado.

## Variante con microservicios

La pantalla Estadísticas ahora consume Reportes por HTTP. Los cálculos están en `microservicios/servicio-estadisticas`; Reportes lo descubre mediante Eureka y Feign.

- Enunciado propuesto: `Enunciado_Microservicios_Lab6.html`.
- Solución actual explicada: `Solucion_Microservicios_Lab6.html`.
- Instrucciones de arranque: `microservicios/README.md`.

`Solucion_Lab6.html` documenta la versión original con cálculos locales; la nueva variante necesita los tres procesos backend encendidos para generar estadísticas.
