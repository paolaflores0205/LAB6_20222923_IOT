# Lab 6: Reportes + Estadísticas + Eureka

Ejercicio propuesto para practicar microservicios; no es un enunciado oficial.

## Documentos

Abre `../Enunciado_Microservicios_Lab6.html` para el ejercicio y `../Solucion_Microservicios_Lab6.html` para la explicación del código y las pruebas.

## Requisitos

- JDK 17 o superior. Se verificó con el JDK 21 de Android Studio.
- Internet en la primera compilación para descargar dependencias.
- Puertos locales libres 8761, 8010 y 8020.
- Maven se descarga mediante el wrapper incluido (3.9.12).

Spring Boot 4.1.2 y Spring Cloud 2025.1.4, como las familias de versiones de la clase. Cada servicio corre como un proceso Java independiente. El POM raíz solo organiza la compilación.

## Compilar y probar

Desde esta carpeta:

```powershell
.\mvnw.cmd clean package
```

Si Java no está configurado en esta PC, selecciona un JDK compatible en tu IDE o configura JAVA_HOME antes de ejecutar Maven. Por ejemplo, si Android Studio está en la ruta habitual:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\mvnw.cmd clean package
```

## Arrancar

Abre tres terminales en esta carpeta y conserva las tres abiertas:

```powershell
# Terminal 1
.\mvnw.cmd -pl eureka-server spring-boot:run
# Terminal 2
.\mvnw.cmd -pl servicio-estadisticas spring-boot:run
# Terminal 3
.\mvnw.cmd -pl servicio-reportes spring-boot:run
```

También puedes abrir los tres archivos `ejecutar-*.cmd`, uno por servicio.

1. Visita http://localhost:8761/ y espera a que aparezcan SERVICIO-ESTADISTICAS y SERVICIO-REPORTES.
2. El registro y su caché tardan en propagarse: si la primera consulta devuelve 503, espera y reintenta.
3. Desde una cuarta terminal ubicada en esta carpeta:

```powershell
curl.exe -i -H "Content-Type: application/json" --data-binary "@ejemplo-solicitud.json" http://localhost:8020/reportes
```

Resultado: 2 sesiones de IoT, total 3.5 horas, noviembre 2026 con 3.5 horas, Teoría 1.5, Práctica 0 y Laboratorio 2.

## Android

Ejecuta la app en modo debug. En Estadísticas configura la dirección base de Reportes (sin `/reportes`):

- Emulador Android: `http://10.0.2.2:8020`.
- Teléfono físico: `http://IP_LOCAL_DE_TU_PC:8020`, teléfono y PC en la misma red; permite el acceso de Java en la red privada si Windows lo solicita.

Pulsa Consultar reporte. El filtro por curso y las actualizaciones de Firestore vuelven a consultar el backend. No hay cálculo local de los totales ni fallback que simule resultados.

La app conserva su Firebase existente. No se requieren credenciales de administrador de Firebase en el backend: recibe sesiones por JSON y no guarda datos. Es una práctica local; los endpoints no validan tokens Firebase. HTTP se habilita únicamente en debug. Una publicación real requiere autenticación del backend y HTTPS.

## Contrato

- Estadísticas: POST http://localhost:8010/estadisticas.
- Reportes: POST http://localhost:8020/reportes.
- Cuerpo: `curso` (null o vacío = todos) y `sesiones` con curso, fecha `yyyy-MM-dd`, horasDedicadas decimales y tipoActividad.
- Respuesta: cantidad, totalHoras, horasPorMes, horasPorActividad y mensaje (preparado por Reportes).
- 400: datos inválidos. 503: Reportes no puede consultar Estadísticas.

## Pruebas

El backend incluye 7 tests de cálculos/validación y 3 tests del controller de Reportes.

La prueba Android `ReporteApiIntegrationTest` es opcional y se omite salvo que actives `probarBackend=true`. Necesita un emulador y los tres servicios en el host. No utiliza sesiones reales de Firebase.

Detén cada proceso con Ctrl+C en su terminal. Los JAR, APK y otras salidas compiladas no se suben al repositorio.
