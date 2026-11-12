# SmartLibrary

API REST académica para gestionar préstamos de libros. Implementada con Java 21, Spring Boot, Spring Data JPA, Validation y H2.

## Requisitos

- JDK 21.
- Maven 3.9 o superior (o utilizar el Maven Wrapper si se agrega).
- Git.

## Ejecutar

Desde la carpeta raíz del proyecto:

```bash
mvn spring-boot:run
```

La API inicia en `http://localhost:8080`.

## Datos de demostración

Se cargan automáticamente:

**Estudiantes**
- ID 1: Ana Gómez
- ID 2: Carlos Pérez

**Libros**
- ID 1: Cien años de soledad
- ID 2: Clean Code
- ID 3: El principito

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/prestamos` | Registrar préstamo |
| GET | `/api/prestamos` | Consultar préstamos |
| GET | `/api/prestamos/{id}` | Consultar préstamo por ID |

### Registrar un préstamo

```http
POST http://localhost:8080/api/prestamos
Content-Type: application/json
```

```json
{
  "estudianteId": 1,
  "libroId": 1
}
```

Respuesta esperada: `201 Created`.

Si el libro ya está prestado: `409 Conflict`.  
Si el estudiante o libro no existe: `404 Not Found`.

## Consola H2

Abrir `http://localhost:8080/h2-console` y utilizar:

- JDBC URL: `jdbc:h2:mem:smartlibrarydb`
- User Name: `sa`
- Password: dejar vacío.

La base de datos está en memoria: los registros se reinician al detener la aplicación.

## Ejecutar pruebas

```bash
mvn test
```

## Diagramas y taller

Los archivos PlantUML y el desarrollo escrito de las actividades están en la carpeta `docs/`.

- `docs/diagrama-clases.puml`
- `docs/diagrama-secuencia.puml`
- `docs/Taller-Resuelto.md`

Puedes abrir los `.puml` con una extensión PlantUML o pegarlos en un visor compatible para exportarlos como PNG o PDF.

## Subir a GitHub

Desde esta carpeta, ejecuta:

```bash
git init
git add .
git commit -m "Initial SmartLibrary implementation"
git branch -M main
git remote add origin https://github.com/AndresDelgadoCas/SmartLibrary-D.git
git push -u origin main
```

Si el repositorio ya tiene un commit inicial creado desde GitHub, primero revisa su contenido y sincroniza antes de hacer el primer push para evitar sobrescribir trabajo.

## Alcance

El proyecto implementa el caso de uso de registrar préstamos y consultar préstamos solicitado en el taller. No incluye todavía altas, edición o eliminación de estudiantes y libros, ni devolución de préstamos, porque no forman parte de los endpoints mínimos pedidos.
