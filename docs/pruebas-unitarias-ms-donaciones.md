# Pruebas Unitarias — ms-donaciones

## Ejecución: `mvn test`

**Fecha:** 2026-06-11  
**Resultado:** BUILD SUCCESS  
**Tiempo total:** 18.261 s

### Métricas de cobertura por suite

| Suite de tests | Tests ejecutados | Fallos | Errores | Omitidos | Tiempo |
|---|---|---|---|---|---|
| `DonacionFactoryTest` | 4 | 0 | 0 | 0 | 0.053 s |
| `CausaServiceTest` | 4 | 0 | 0 | 0 | 1.317 s |
| `DonacionServiceTest` | 5 | 0 | 0 | 0 | 0.184 s |
| `TestimonioServiceTest` | 2 | 0 | 0 | 0 | 0.072 s |
| **TOTAL** | **15** | **0** | **0** | **0** | — |

---

## Tabla de tests por clase

### DonacionFactoryTest

| Test | Clase testeada | Resultado esperado |
|---|---|---|
| `creaMonetariaConMontoPositivo` | `DonacionFactory` | Donación MONETARIA creada con monto 50.000 |
| `monetariaConMontoNuloLanzaExcepcion` | `DonacionFactory` | Lanza `IllegalArgumentException` si monto es null |
| `creaRopaConMontoEnCero` | `DonacionFactory` | Donación ROPA creada con monto 0 |
| `creaAlimentoConValorEstimado` | `DonacionFactory` | Donación ALIMENTO creada con fecha no nula |

### DonacionServiceTest

| Test | Clase testeada | Resultado esperado |
|---|---|---|
| `testCrearDonacion_valida_retornaDonacion` | `DonacionService` | Donación MONETARIA creada, estado PENDIENTE, se llama `actualizarRecaudado` |
| `testCrearDonacion_causaNoExiste_lanzaException` | `DonacionService` | Lanza `EntityNotFoundException` cuando la causa no existe |
| `testActualizarEstado_donacionExiste_actualizaEstado` | `DonacionService` | Estado cambia a COMPLETADA y se persiste |
| `testActualizarEstado_donacionNoExiste_lanza404` | `DonacionService` | Lanza `EntityNotFoundException` cuando el id no existe |
| `testListarDonaciones_retornaLista` | `DonacionService` | Retorna lista con 2 donaciones |

### CausaServiceTest

| Test | Clase testeada | Resultado esperado |
|---|---|---|
| `testCrearCausa_valida_retornaCausa` | `CausaService` | Causa creada con nombre y meta correctos |
| `testObtenerCausa_existente_retornaCausa` | `CausaService` | Retorna la causa con id=1 |
| `testObtenerCausa_noExiste_lanza404` | `CausaService` | Lanza `EntityNotFoundException` cuando id no existe |
| `testListarCausas_retornaLista` | `CausaService` | Retorna lista de 2 causas activas |

### TestimonioServiceTest

| Test | Clase testeada | Resultado esperado |
|---|---|---|
| `testCrearTestimonio_valido_retornaTestimonio` | `TestimonioService` | Testimonio creado con titulo, autorId, autorNombre y fechaCreacion no nulos |
| `testListarTestimonios_retornaLista` | `TestimonioService` | Retorna lista con 2 testimonios |

---

## Tecnologías utilizadas

- **JUnit 5** (`@ExtendWith(MockitoExtension.class)`, `@Test`)
- **Mockito** (`@Mock`, `@InjectMocks`, `when()`, `verify()`, `thenAnswer()`)
- **Spring Boot Test** 3.2.5
