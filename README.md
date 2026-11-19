# Post-contenido — Unidad 6: Antipatrones de Diseño

## Descripción
Repositorio del post-contenido de la Unidad 6 de Patrones de Diseño de Software — Sexto Semestre.
Un único proyecto Spring Boot (`pedidos-service/`) que aborda dos fases fundamentales del ciclo de vida del software:
1. **Parte 1:** Diagnóstico y refactorización de un antipatrón combinado (**God Object** y **Spaghetti Code**) en la clase central `GestorPedidos`.
2. **Parte 2:** Diagnóstico y corrección de un segundo antipatrón (**Golden Hammer**) introducido durante un ciclo de crecimiento rápido al implementar tres campañas de descuento promocional.

---

## Decisiones de diseño

### Parte 1 — GestorPedidos
- **Antipatrón identificado:** **God Object (Objeto Todopoderoso / Blob)** y **Spaghetti Code (Código Espagueti)** combinados.
  - **Evidencia concreta en el código inicial:**
    - `GestorPedidos.procesarPedido()` concentraba seis (6) responsabilidades dispares en un único método procedimental de más de 110 líneas (y 340 líneas en toda la clase incluyendo métodos auxiliares de facturación, auditoría e impuestos):
      1. *Líneas 34-47:* Validación de stock acoplada a consultas directas JDBC contra la tabla `inventario`.
      2. *Líneas 49-69:* Validación de existencia y morosidad de cliente, mezclada con lógica temporal de horario de corte (`20:00`) y consultas a `facturas`.
      3. *Líneas 71-78:* Cálculo de subtotal realizando una consulta SQL unitaria por cada ítem del carrito.
      4. *Líneas 80-99:* Cálculo de descuentos con hasta 3 niveles de anidamiento condicional (`if/else`) según tipología (`VIP`, `FRECUENTE`) y montos.
      5. *Líneas 104-121:* Persistencia directa relacional sin repositorio ni delimitación transaccional (`INSERT INTO pedidos`, `CALL IDENTITY()`, `INSERT INTO detalle_pedido`, `UPDATE inventario`).
      6. *Líneas 122-137:* Formateo de presentación de correo en texto plano y despacho mediante `EmailService`.
- **Patrones de diseño aplicados:**
  - **Chain of Responsibility (`ValidadorPedido`, `ValidadorStock`, `ValidadorCliente`):** Se eligió para la secuencia de validaciones debido a que existe una dependencia real de orden y corte anticipado (*fail-fast*): si el stock falla, no tiene sentido consultar las facturas en base de datos.
  - **Strategy (`EstrategiaDescuento`, `DescuentoVip`, `DescuentoFrecuente`, `DescuentoEstandar`, `SelectorEstrategiaDescuento`):** Se eligió para el cálculo de descuentos por cliente porque son políticas de evaluación mutuamente excluyentes sin dependencia secuencial.
  - **Repository (`PedidoRepository`):** Aísla las sentencias SQL y la persistencia relacional.
  - **Service (`NotificacionPedidoService`):** Desacopla la construcción y envío del correo de confirmación.
  - **Orquestador Delgado (`GestorPedidos`):** Se redujo a coordinar las cuatro capas cohesivas sin conocer detalles internos.
- **Alternativa descartada:** Un método único `validarTodo()` invocando una colección de `Predicate<ContextoPedido>`. Se descartó porque evalúa todas las condiciones sin corte anticipado nativo, forzando consultas a base de datos innecesarias cuando un paso anterior ya rechazó el pedido.

---

### Parte 2 — Crecimiento del proyecto
- **Antipatrón identificado:** **Golden Hammer (Martillo de Oro)**.
  - **Evidencia concreta en el código intermedio:**
    - Se agregaron tres nuevas campañas comerciales (`PromocionBlackFriday`, `PromocionCorporativo`, `PromocionVolumen`) como subclases de `ValidadorPedido` dentro de la misma cadena.
    - *Ausencia de dependencia de orden:* A diferencia de `ValidadorStock` y `ValidadorCliente`, ejecutar `PromocionVolumen` antes de `PromocionCorporativo` produce exactamente el mismo resultado.
    - *Violación del contrato semántico (LSP):* `ValidadorPedido` fue diseñado para decidir si el pedido continúa o se rechaza (`contexto.rechazar(...)`). Los nuevos eslabones nunca rechazan nada; únicamente se colgaron de la cadena para mutar un campo compartido.
    - *Corrupción del estado mutable:* Se introdujo artificialmente `descuentoCampana` en `ContextoPedido`, forzando a los eslabones a competir por sobrescribir un atributo mutable.
    - *Causa raíz:* Se adoptó *Chain of Responsibility* únicamente porque "ya existía y había funcionado bien en la Parte 1", sin evaluar la forma del nuevo problema.
- **Patrón aplicado:** **Strategy** extensible mediante `CalculadorDescuentoFinal`.
  - Las tres campañas se modelaron como implementaciones de `EstrategiaDescuento` (`DescuentoBlackFriday`, `DescuentoCorporativo`, `DescuentoVolumen`).
  - `CalculadorDescuentoFinal` evalúa la estrategia del tipo de cliente y las campañas concurrentes activas, aplicando de forma limpia la regla de negocio `Math.max(porTipoCliente, porCampana)`.
  - **Prevención de Lava Flow:** Las clases transitorias `PromocionBlackFriday`, `PromocionCorporativo` y `PromocionVolumen`, así como el campo mutable `descuentoCampana`, fueron **eliminados por completo del código fuente** (no se dejaron comentados). El historial de Git es el único lugar responsable de documentar dicha transición evolutiva.
- **Alternativa descartada:** Mantener las campañas como eslabones de la cadena agregando banderas de control o acumuladores. Se descartó formalmente porque consolida el Golden Hammer, contaminando la responsabilidad de validación con cálculos tarifarios.

---

## Comparación Antes / Después del Diseño

| Dimensión | Versión Inicial (God Object / Spaghetti) | Versión Intermedia (Golden Hammer) | Versión Final Refactorizada |
| :--- | :--- | :--- | :--- |
| **Responsabilidades en `GestorPedidos`** | 6 responsabilidades mezcladas en 1 método (~140 líneas) | Orquestador + 5 eslabones forzados en cadena | Orquestador puro de ~60 líneas delegando en 4 capas |
| **Flujo de Validaciones** | Procedimental con consultas SQL directas | Cadena contaminada con promociones | Cadena pura con corte anticipado (`Stock -> Cliente`) |
| **Cálculo de Descuentos** | 3 niveles de condicionales anidados | Banderas y mutación de estado en cadena | Patrón Strategy composable con `CalculadorDescuentoFinal` |
| **Persistencia** | SQL embebido en el servicio de negocio | Repositorio desacoplado | Repositorio desacoplado (`PedidoRepository`) |
| **Notificaciones** | Construcción de Strings en el flujo principal | Servicio desacoplado | Servicio desacoplado (`NotificacionPedidoService`) |
| **Extensibilidad (OCP)** | Requiere modificar el método central | Requiere crear eslabones falsos en la cadena | Agregar una nueva estrategia sin tocar código existente |

---

## Cómo ejecutar

### Requisitos previos
- Java JDK 17 o superior instalado.
- Maven 3.8+ instalado.

### Comandos de ejecución y pruebas
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la suite completa de pruebas unitarias y de integración
mvn test

# Ejecutar el servicio Spring Boot localmente
mvn spring-boot:run
```

Consola de Base de Datos H2 (en modo de desarrollo):
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:pedidosdb`
- Usuario: `sa` | Contraseña: *(vacía)*

---

## Herramientas utilizadas
- **Lenguaje:** Java 17 LTS
- **Framework:** Spring Boot 3.2.0 (Spring Web, Spring JDBC API)
- **Base de Datos Embebida:** H2 Database Engine (en memoria)
- **Pruebas y Mocks:** JUnit 5 (Jupiter), Mockito, Spring Boot Test
- **Control de Versiones y Gestión:** Git, GitHub

---

## Conclusiones
1. El diagnóstico riguroso de antipatrones como **God Object** y **Spaghetti Code** demuestra que concentrar múltiples niveles de abstracción y responsabilidades en una sola clase incrementa exponencialmente la fragilidad del software y dificulta la detección de errores de dominio.
2. La separación en capas mediante patrones como **Chain of Responsibility** para procesos secuenciales con corte anticipado y **Strategy** para algoritmos intercambiables de pricing restaura el cumplimiento de los principios SOLID (SRP, OCP y LSP).
3. El antipatrón **Golden Hammer** suele gestarse a partir de éxitos previos: asumir que una solución que funcionó bien para un problema debe aplicarse a cualquier requerimiento subsiguiente sin evaluar la naturaleza del dominio degrada la arquitectura.
4. Identificar y eliminar de raíz el código descartado en lugar de dejarlo comentado previene la formación de **Lava Flow**, asegurando que el repositorio permanezca limpio, comprensible y sostenible para futuros ciclos de mantenimiento.
