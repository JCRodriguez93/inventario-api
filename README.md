# inventario-api

API REST de inventario: referencias (artículos), stock multi-almacén, movimientos
de entrada/salida y alertas de reposición. Spring Boot 4 + JPA + contrato OpenAPI 3.

## Procedencia

Generado con [Scafftrix](https://github.com/JCRodriguez93/Scafftrix) desde su
perfil **`SPRING_BOOT`** (monolito modular) y adaptado a un dominio real:

- El contrato `src/main/resources/contract/api.yaml` es la fuente de verdad.
  De él se generan con openapi-generator los modelos (`org.openapitools.model`)
  y las interfaces de los controllers (`org.openapitools.api`).
- **No hay DTOs escritos a mano.** Entre el contrato y JPA solo hay entidades
  (`*Entity`) y mappers MapStruct (`*Mapper`).
- Los módulos no se tocan entre sí: `movimientos` y `alertas` consumen
  `stock` únicamente a través de la interfaz pública `stock.api.StockConsulta`.

## Requisitos

- Java 17 y Maven 3.9+
- Docker (solo para ejecutar contra MySQL)

## Cómo ejecutarlo

```bash
# 1. base de datos (MySQL 8 en el puerto 3306)
docker compose up -d

# 2. API contra MySQL
mvn spring-boot:run -Dspring-boot.run.profiles=mysql

# ...o contra H2 en memoria, sin Docker
mvn spring-boot:run

# tests (unitarios con Mockito + integración MockMvc sobre H2)
mvn test
```

La API queda en `http://localhost:8080/api/v1/...` y arranca con datos de
ejemplo (3 referencias, 2 almacenes) la primera vez que encuentra la base vacía.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/v1/referencias` | Lista las referencias |
| `POST` | `/api/v1/referencias` | Crea una referencia (201; 409 si el SKU ya existe) |
| `PUT` | `/api/v1/referencias/{sku}` | Actualiza una referencia (404 si no existe) |
| `GET` | `/api/v1/stock/{sku}` | Cantidades de una referencia en cada almacén (404) |
| `GET` | `/api/v1/movimientos` | Historial paginado (`page`, `size`) |
| `POST` | `/api/v1/movimientos` | Registra una entrada o salida (201; **409 si no hay stock**) |
| `GET` | `/api/v1/movimientos/{id}` | Detalle de un movimiento (404) |
| `GET` | `/api/v1/alertas` | Referencias por debajo de su stock mínimo |

Errores: cuerpo `ApiError` (`status`, `error`, `message`) resuelto por el
`@RestControllerAdvice` del paquete `config`.

## Estructura

```
es.scafftrix.inventarioapi
├── stock/        # referencias y niveles por almacén
│   ├── api/      # StockConsulta: frontera pública del módulo
│   ├── controller/ entity/ exception/ mapper/ repository/ service/
├── movimientos/  # entradas, salidas e historial
├── alertas/      # regla de stock mínimo (solo usa StockConsulta)
└── config/       # manejo global de errores + datos de ejemplo
```
