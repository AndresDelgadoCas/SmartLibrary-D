# Taller guiado SmartLibrary — Desarrollo propuesto

**Asignatura:** Diseño de Software  
**Tema:** Del modelo orientado a objetos a una API REST por tres capas  
**Tecnología de referencia:** Java 21, Spring Boot, Spring Data JPA y H2.

> Este documento contiene una propuesta de solución para las actividades de diseño. Las evidencias de ejecución deben obtenerse al correr y probar el proyecto en el equipo.

## Actividad 1. Leer y cuestionar el modelo

### Datos y operaciones del dominio

- **Estudiante:** identificador, nombre e identificación. Se consulta para comprobar que quien solicita el préstamo existe.
- **Libro:** identificador, título, autor y disponibilidad. Su estado cambia cuando se presta.
- **Prestamo:** identificador, estudiante asociado, libro asociado y fecha del préstamo. La fecha de devolución puede quedar vacía mientras el préstamo está activo.
- **GestorPrestamos:** en el modelo inicial coordina las colecciones y operaciones. En el rediseño sus responsabilidades se distribuyen entre Controller, Service y Repository.

### ¿Por qué Estudiante–Prestamo y Libro–Prestamo son asociaciones?

Son asociaciones porque un préstamo mantiene referencias a un estudiante y a un libro. No son composiciones: el estudiante y el libro existen independientemente del préstamo y no deben eliminarse cuando se elimina o deja de existir un préstamo. Además, un estudiante y un libro pueden participar en distintos préstamos históricos.

### Operaciones necesarias

1. Buscar estudiante por identificador.
2. Buscar libro por identificador.
3. Comprobar que el libro esté disponible y que no tenga un préstamo activo.
4. Crear el préstamo con la fecha actual.
5. Cambiar la disponibilidad del libro a falso.
6. Guardar el préstamo y el cambio del libro en una misma transacción.
7. Consultar todos los préstamos y consultar uno por identificador.

### Responsabilidades que faltan

El modelo inicial no define una entrada HTTP, por lo que falta un Controller que reciba solicitudes y construya respuestas. Tampoco separa la coordinación y reglas del caso de uso, responsabilidad que corresponde a Service. Finalmente, hacen falta Repository para consultar y persistir las entidades.

## Actividad 2. Asignar responsabilidades

| Operación o decisión | Clase responsable | Justificación |
|---|---|---|
| Recibir `POST /api/prestamos` | PrestamoController | Es el punto de entrada HTTP; recibe y valida la estructura de la solicitud. |
| Buscar estudiante por identificador | EstudianteRepository, invocado por PrestamoService | El repositorio consulta la persistencia; el servicio coordina el caso de uso. |
| Buscar libro por identificador | LibroRepository, invocado por PrestamoService | Encapsula el acceso a datos del libro. |
| Validar disponibilidad | PrestamoService | Es una regla de negocio del caso de uso, no una responsabilidad HTTP ni de persistencia. |
| Crear el préstamo | PrestamoService crea la entidad Prestamo | El servicio coordina la creación después de validar las condiciones. |
| Cambiar disponibilidad | PrestamoService solicita el cambio sobre Libro | El servicio aplica la regla del caso de uso y modifica el estado del objeto de dominio. |
| Guardar cambios | LibroRepository y PrestamoRepository | Los repositorios persisten el libro actualizado y el nuevo préstamo. La operación está dentro de una transacción del servicio. |
| Devolver respuesta HTTP | PrestamoController | Traduce el resultado del servicio a `201 Created`, `200 OK` u otros códigos HTTP. |

## Actividad 3. Modelo de clases por capas

El diagrama PlantUML está en `diagrama-clases.puml`.

### Clases principales

- `PrestamoController`: recibe solicitudes REST y delega en el servicio.
- `PrestamoService`: coordina el caso de uso, valida reglas y define la transacción.
- `PrestamoRepository`, `LibroRepository`, `EstudianteRepository`: interfaces de persistencia que extienden `JpaRepository`.
- `Estudiante`, `Libro`, `Prestamo`: entidades del dominio persistidas mediante JPA.
- `PrestamoRequest`: DTO de entrada con los identificadores del estudiante y del libro.
- `PrestamoResponse`: DTO de salida para no exponer directamente las entidades JPA.

### Relaciones y multiplicidades

- Un `Estudiante` puede estar asociado con cero o muchos `Prestamo`; cada `Prestamo` corresponde a exactamente un `Estudiante`.
- Un `Libro` puede estar asociado con cero o muchos `Prestamo` históricos; cada `Prestamo` corresponde a exactamente un `Libro`.
- Controller depende de Service; Service depende de los tres Repository. Son dependencias de uso, no asociaciones de dominio.
- Cada Repository es una interfaz que hereda las operaciones de `JpaRepository`; no se dibuja una asociación persistente desde el servicio hacia las entidades por el simple hecho de recibirlas temporalmente.

No se emplean rombos de agregación o composición porque las entidades tienen ciclos de vida independientes.

## Actividad 4. Diagrama de secuencia

El diagrama está en `diagrama-secuencia.puml`. Contiene:
- Camino exitoso: búsqueda de estudiante y libro, validación, actualización de disponibilidad, persistencia y respuesta `201 Created`.
- Camino de error: estudiante o libro inexistente produce `404 Not Found`; libro no disponible o con préstamo activo produce `409 Conflict`.

Los mensajes corresponden a operaciones existentes en las clases y repositorios.

## Actividad 5. Preparación del proyecto

El proyecto utiliza Java 21, Maven, Spring Web, Spring Data JPA, Validation y H2. Los paquetes son `controller`, `service`, `repository`, `model`, `dto`, `exception` y `config`.

Se agrega `exception` para centralizar el manejo de errores y `config` para cargar datos de demostración. Esto no cambia la arquitectura principal de tres capas.

## Actividad 6. Pruebas y verificación

| Escenario | Resultado esperado | Cómo verificar |
|---|---|---|
| Préstamo de libro disponible | `201 Created` y préstamo registrado | Enviar un POST con estudiante 1 y libro 1; consultar luego los préstamos. |
| Libro ya prestado | `409 Conflict`; sin segundo préstamo | Repetir el POST para el mismo libro sin devolución. |
| Estudiante inexistente | `404 Not Found` | Enviar un identificador de estudiante que no exista, por ejemplo 999. |
| Libro inexistente | `404 Not Found` | Enviar un identificador de libro que no exista, por ejemplo 999. |
| Consulta de préstamos | `200 OK` con registros | Ejecutar GET `/api/prestamos`. |

### Solicitud de prueba

`POST http://localhost:8080/api/prestamos`

```json
{
  "estudianteId": 1,
  "libroId": 1
}
```

Consulta de todos los préstamos:

`GET http://localhost:8080/api/prestamos`

Consulta de un préstamo:

`GET http://localhost:8080/api/prestamos/1`

### Datos iniciales

Al iniciar, la aplicación crea dos estudiantes y tres libros para facilitar las pruebas. Los identificadores iniciales esperados son estudiantes 1 y 2; libros 1, 2 y 3.

### Consideraciones de consistencia

El método de registro utiliza `@Transactional`: si falla la operación, se revierte la transacción. La entidad Libro incluye `@Version` para detectar actualizaciones concurrentes y responder con conflicto ante una actualización optimista fallida. Para sistemas con concurrencia alta se puede profundizar en bloqueos de base de datos y restricciones adicionales.

**Importante:** H2 está configurada en memoria (`jdbc:h2:mem`). Los datos se reinician al detener la aplicación. Esto es adecuado para la demostración indicada en el taller.

## Actividad 7. Revisión y defensa

### ¿Qué responsabilidades cambiaron?

El modelo inicial concentraba la gestión en `GestorPrestamos`. En el nuevo diseño, Controller recibe las solicitudes HTTP, Service coordina las reglas del caso de uso y Repository encapsula la persistencia. Las entidades conservan los datos y el comportamiento propio del dominio.

### ¿Qué patrón GRASP se reconoce?

Se reconoce principalmente **Controller**, porque `PrestamoController` recibe el evento del sistema relacionado con registrar un préstamo y delega el trabajo. También se aplica **Information Expert** al mantener los datos y operaciones de disponibilidad en `Libro`, mientras el servicio coordina el caso de uso. El controlador GRASP es un concepto de asignación de responsabilidades; `@RestController` es una anotación concreta de Spring para construir controladores REST. No son conceptos equivalentes.

### ¿Dónde se ubica la regla de disponibilidad?

La validación del caso de uso se coordina en `PrestamoService`, porque necesita consultar el libro y los préstamos activos antes de registrar. El estado de disponibilidad pertenece a `Libro`, que ofrece operaciones para cambiarlo.

### ¿Qué relaciones se conservaron o modificaron?

Se conservaron las asociaciones históricas entre Estudiante–Prestamo y Libro–Prestamo. Se reemplazó la coordinación concentrada del gestor por dependencias entre Controller, Service y Repository. No se añadieron composiciones porque no existe dependencia de ciclo de vida.

### ¿Cómo se comprueba la trazabilidad?

- Requisito R1 y R5 → `PrestamoService.registrarPrestamo` y consulta de préstamos activos → prueba de libro ya prestado.
- Requisito R2 → entidad `Prestamo` y sus relaciones/fecha → prueba de préstamo exitoso.
- Requisito R3 → `Libro.marcarComoPrestado` → consulta del estado del libro o inspección en H2.
- Requisito R4 → validaciones y excepciones antes de guardar → pruebas 404 y 409 sin nuevo préstamo.

## Explicación breve de decisiones de diseño

Se adoptó una arquitectura de tres capas para separar responsabilidades y reducir el acoplamiento. El Controller conoce al Service, pero no accede directamente a los repositorios. El Service coordina el caso de uso y concentra las reglas que requieren consultar varias entidades. Los Repository abstraen la persistencia mediante Spring Data JPA. Las entidades Estudiante, Libro y Prestamo representan el dominio y mantienen relaciones de asociación, no de composición, porque sus ciclos de vida son independientes.

El registro de préstamo se ejecuta en una transacción para que la creación del préstamo y el cambio de disponibilidad del libro se confirmen o reviertan conjuntamente. Se utiliza H2 en memoria para la demostración y DTO para separar el contrato HTTP de las entidades persistentes. Las respuestas HTTP diferencian creación exitosa (`201`), consulta (`200`), recurso inexistente (`404`) y conflicto de negocio (`409`).
