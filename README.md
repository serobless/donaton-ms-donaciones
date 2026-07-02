# ms-donaciones

Microservicio de gestión de donaciones para la plataforma Donaton. Forma parte de un sistema de microservicios (gateway en puerto 8082, frontend en 5173).

## ¿Qué hace?

- CRUD de **causas** (campañas de donación).
- Registro y consulta de **donaciones** (monetarias y en especie: ropa, alimentos, medicamentos), con soporte para donaciones empresariales e ítems múltiples por donación.
- Gestión de **centros de acopio** con geocodificación automática vía Nominatim.
- **Testimonios** de donantes con flujo de aprobación.
- **Necesidades** por centro de acopio.
- Upload y servicio de **imágenes**.
- Endpoints públicos de transparencia y estadísticas.

## Requisitos

- Java 21
- Maven 3.9+
- MySQL 8 corriendo en `localhost:3306`

## Base de datos

```sql
CREATE DATABASE donaton_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configura usuario y contraseña en `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=tu_password
```

El esquema se genera automáticamente con `spring.jpa.hibernate.ddl-auto=update`.

## Cómo correrlo

```bash
mvn spring-boot:run
```

Levanta en el puerto **8084**. Al arrancar con la base vacía, `DataInitializer` carga 3 causas y 15 donaciones de prueba.

## Endpoints principales

### Causas (público GET, ADMIN POST/PUT/DELETE)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/causas | Listar causas activas |
| GET | /api/causas/{id} | Detalle de una causa |
| POST | /api/causas | Crear causa |
| PUT | /api/causas/{id} | Actualizar causa |
| DELETE | /api/causas/{id} | Eliminar causa |

### Donaciones
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/donaciones | Crear donación (autenticado) |
| GET | /api/donaciones | Listar todas (ADMIN) |
| GET | /api/donaciones/mis-donaciones | Donaciones del usuario (autenticado) |
| GET | /api/donaciones/top-donadores | Top 10 donadores (público) |
| GET | /api/donaciones/transparencia | Listado público auditado |
| GET | /api/donaciones/ultimas | Últimas N donaciones (público) |
| GET | /api/donaciones/total | Total recaudado (público) |
| GET | /api/donaciones/count | Conteo de donaciones (público) |
| PATCH | /api/donaciones/{id}/estado | Cambiar estado |
| DELETE | /api/donaciones/{id} | Eliminar (ADMIN) |

### Testimonios
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/testimonios | Listar testimonios aprobados (público) |
| POST | /api/testimonios | Crear testimonio (autenticado) |
| GET | /api/testimonios/pendientes | Pendientes de aprobación (ADMIN) |
| PUT | /api/testimonios/{id}/aprobar | Aprobar testimonio (ADMIN) |

### Centros de acopio
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/centros | Listar centros activos (público) |
| GET | /api/centros/{id} | Detalle (público) |
| POST | /api/centros | Crear centro con geocodificación (ADMIN) |
| PUT | /api/centros/{id} | Actualizar (ADMIN) |
| DELETE | /api/centros/{id} | Eliminar (ADMIN) |

### Imágenes
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/imagenes/upload | Subir imagen, guarda en `uploads/` (ADMIN) |
| GET | /api/imagenes/uploads/{nombre} | Servir imagen (público) |

## Documentación interactiva

Disponible en `http://localhost:8084/swagger-ui.html` una vez levantado el servicio.
