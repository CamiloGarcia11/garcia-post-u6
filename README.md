# Post-contenido — Unidad 6: Antipatrones de Diseño

## Descripción
Repositorio del post-contenido de la Unidad 6 de Patrones de Diseño de Software — Sexto Semestre.
Un único proyecto Spring Boot (`pedidos-service/`) con dos partes: diagnóstico y refactorización de un antipatrón combinado en `GestorPedidos`, y diagnóstico y corrección de un segundo antipatrón introducido al hacer crecer el mismo proyecto con tres campañas de descuento.

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

---

## Diagnóstico de Antipatrones (Parte 2: Crecimiento del Sistema)

### 1. Antipatrón Identificado: Golden Hammer (Martillo de Oro)
Al incorporar las tres nuevas campañas de descuento (`PromocionBlackFriday`, `PromocionCorporativo`, `PromocionVolumen`), se incurre en el antipatrón **Golden Hammer**.
Habiendo experimentado el éxito de *Chain of Responsibility* para desacoplar `ValidadorStock` y `ValidadorCliente` en la Parte 1, el desarrollador reutilizó indiscriminadamente este mismo patrón estructural para un problema con características semánticas y operativas totalmente distintas.

### 2. Evidencia Concreta del Código Fuente
- **Ausencia total de dependencia de orden y de necesidad de corte anticipado**:
  - A diferencia de `ValidadorStock` y `ValidadorCliente` (donde si el stock no existe, carece de sentido consultar las facturas del cliente), las promociones no guardan relación de precedencia. Ejecutar `PromocionVolumen` antes de `PromocionCorporativo` o `PromocionBlackFriday` produce idéntico resultado.
- **Ruptura del contrato semántico y de abstracción (`Liskov Substitution Principle`)**:
  - La clase abstracta `ValidadorPedido` define conceptualmente la responsabilidad de *validar* y potencialmente *rechazar* un pedido (`contexto.rechazar(...)`).
  - Sin embargo, `PromocionBlackFriday`, `PromocionCorporativo` y `PromocionVolumen` **nunca rechazan un pedido**. Únicamente aprovechan que la infraestructura de la cadena existe para engancharse como parásitos y mutar un estado compartido.
- **Corrupción del Contexto con Estado Mutable Compartido**:
  - Se forzó la inserción de un campo mutable adicional en `ContextoPedido` (`descuentoCampana` y `aplicarDescuentoCampana(double)`), generando competencia por efectos secundarios entre eslabones.
- **Dificultad de composición y evolución de negocio**:
  - Si el negocio cambia la regla de "el mayor descuento gana" por una de acumulación aditiva, la cadena queda incapacitada para manejarlo limpiamente sin lógica arbitral invasiva.

---

## Decisiones de diseño

### Parte 1 — GestorPedidos
- **Antipatrón identificado:** God Object y Spaghetti Code combinados en `GestorPedidos.procesarPedido()`.
- **Patrón aplicado:**
  - **Chain of Responsibility** para la secuencia de validaciones: existe una dependencia estricta de orden y necesidad de corte anticipado (si el stock falla, no se debe consultar la mora del cliente).
  - **Strategy** para las políticas de descuento por tipo de cliente: las reglas son mutuamente excluyentes y no dependen de un orden procedural.
  - **Repository** (`PedidoRepository`) para aislar la persistencia JDBC.
  - **Service dedicado** (`NotificacionPedidoService`) para la construcción y envío del correo de confirmación.
- **Alternativa descartada:** Un método monolítico `validarTodo()` con una lista de `Predicate<ContextoPedido>`. Se descartó porque evalúa condiciones sin corte anticipado elegante y obliga a instanciar verificaciones innecesarias cuando un paso previo ya determinó el rechazo.

### Parte 2 — Crecimiento del proyecto
- **Antipatrón identificado:** Golden Hammer. Las tres campañas de descuento se implementaron como eslabones de la cadena de validación existente, violando el contrato de la cadena y acoplando responsabilidades de cálculo de precios dentro de la etapa de validación.
- **Patrón a aplicar:** **Strategy**, extendiendo la interfaz `EstrategiaDescuento` con `DescuentoBlackFriday`, `DescuentoCorporativo` y `DescuentoVolumen`, coordinados por un `CalculadorDescuentoFinal`. Los tres eslabones incorrectos y el campo `descuentoCampana` se eliminan por completo del código fuente (evitando un **Lava Flow**).
- **Alternativa descartada:** Mantener las promociones dentro de la cadena con un mecanismo de banderas booleanas. Se descarta porque perpetúa el antipatrón al usar una herramienta para fines opuestos a su naturaleza de diseño.

---

## Cómo ejecutar
```bash
mvn spring-boot:run
mvn test
```

## Herramientas utilizadas
- Java 17, Spring Boot 3.2.0, Spring JDBC, H2 Database
- Git, GitHub
