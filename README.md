# Post-contenido — Unidad 6: Antipatrones de Diseño

## Descripción
Repositorio del post-contenido de la Unidad 6 de Patrones de Diseño de Software — Sexto Semestre.
Un único proyecto Spring Boot (`pedidos-service/`) que aborda el diagnóstico y refactorización de antipatrones de diseño en un sistema de gestión de pedidos de comercio electrónico.

---

## Diagnóstico de Antipatrones (Parte 1: GestorPedidos)

### 1. Antipatrones Identificados
En la versión inicial de `GestorPedidos.java` se diagnostica la combinación de dos antipatrones fundamentales:
1. **God Object (Objeto Todopoderoso / Blob)**: La clase asume el control omnisciente de todo el ciclo de vida del pedido, acumulando más de 6 responsabilidades dispares y múltiples niveles de abstracción.
2. **Spaghetti Code (Código Espagueti)**: El método `procesarPedido(PedidoRequest request)` entremezcla flujos de control procedimentales, validaciones con dependencias temporales, consultas SQL intercaladas, múltiples niveles de anidamiento condicional y construcción manual de cadenas de notificación.

### 2. Evidencia Concreta del Código Fuente
Analizando `GestorPedidos.java`, se identifican las siguientes evidencias técnicas:

- **Mezcla de seis (6) responsabilidades dispares en un único método (`procesarPedido`)**:
  - **Líneas 34-47 (Validación de Stock)**: Inspección de ítems y consultas JDBC directas a la tabla `inventario`.
  - **Líneas 49-69 (Validación de Cliente y Mora)**: Verificación de existencia del cliente, consulta de facturas pendientes con excepción basada en horario de corte (`LocalTime.of(20, 0)`).
  - **Líneas 71-78 (Cálculo de Subtotal)**: Lógica de cotización con consulta SQL unitaria por cada producto (`SELECT precio FROM productos WHERE id = ?`).
  - **Líneas 80-99 (Cálculo de Descuentos)**: Reglas de negocio anidadas con hasta 3 niveles de bifurcación (`if/else`) según tipo de cliente (`VIP`, `FRECUENTE`) y montos/pedidos acumulados.
  - **Líneas 104-121 (Persistencia de Datos)**: Operaciones DML directas (`INSERT INTO pedidos`, obtención de identidad con `CALL IDENTITY()`, inserción en `detalle_pedido` y actualización de `inventario`) sin transacción declarativa ni abstracción de repositorio.
  - **Líneas 122-137 (Construcción y Envío de Notificación)**: Formateo de cadenas de texto plano para el correo electrónico y manejo manual de excepciones.

- **Múltiples niveles de anidamiento condicional**:
  - El bloque de cálculo de descuentos (líneas 82-99) alcanza 3 niveles de anidamiento (`if (tipoCliente.equals("VIP")) -> if (subtotal > 1_000_000) ... else if (subtotal > 500_000) ...`).
  - La validación de cliente moroso (líneas 56-68) anida la verificación de tipo, la existencia de deuda pendiente y la condición temporal de hora límite.

- **Colisión de niveles de abstracción**:
  - En la misma secuencia procedural conviven instrucciones de bajo nivel de acceso a datos (`jdbcTemplate.queryForObject(...)`), lógica de dominio financiero (impuestos y descuentos) y concatenación de mensajes de presentación para el usuario.

- **Violación de principios de diseño**:
  - **Single Responsibility Principle (SRP)**: La clase tiene al menos 6 razones para cambiar (cambio en inventario, política de morosidad, política de descuentos, esquema de base de datos, motor de notificación o formato del mensaje).
  - **Open/Closed Principle (OCP)**: Agregar una nueva tipología de cliente o campaña promocional exige modificar directamente el cuerpo del método principal de `GestorPedidos`.

---

## Decisiones de diseño

### Parte 1 — GestorPedidos
- **Antipatrón identificado:** God Object y Spaghetti Code combinados en `GestorPedidos.procesarPedido()`.
- **Patrón a aplicar:**
  1. **Chain of Responsibility** para la secuencia de validaciones: existe una dependencia estricta de orden y necesidad de corte anticipado (si el stock falla, no se debe consultar la mora del cliente).
  2. **Strategy** para las políticas de descuento por tipo de cliente: las reglas son mutuamente excluyentes y no dependen de un orden procedural.
  3. **Repository** (`PedidoRepository`) para aislar la persistencia JDBC.
  4. **Service dedicado** (`NotificacionPedidoService`) para la construcción y envío del correo de confirmación.
- **Alternativa descartada:** Un método monolítico `validarTodo()` con una lista de `Predicate<ContextoPedido>`. Se descartó porque evalúa condiciones sin corte anticipado elegante y obliga a instanciar verificaciones innecesarias cuando un paso previo ya determinó el rechazo.

---

## Cómo ejecutar
```bash
mvn spring-boot:run
mvn test
```

## Herramientas utilizadas
- Java 17, Spring Boot 3.2.0, Spring JDBC, H2 Database
- Git, GitHub
