-- Productos
INSERT INTO productos (id, nombre, precio) VALUES (1, 'Teclado Mecanico', 100000.0);
INSERT INTO productos (id, nombre, precio) VALUES (2, 'Mouse Gamer', 50000.0);
INSERT INTO productos (id, nombre, precio) VALUES (3, 'Monitor 4K', 1200000.0);
INSERT INTO productos (id, nombre, precio) VALUES (4, 'Memoria RAM 16GB', 250000.0);

-- Inventario
INSERT INTO inventario (producto_id, stock) VALUES (1, 100);
INSERT INTO inventario (producto_id, stock) VALUES (2, 5); -- stock limitado para pruebas de insuficiencia
INSERT INTO inventario (producto_id, stock) VALUES (3, 50);
INSERT INTO inventario (producto_id, stock) VALUES (4, 100);

-- Clientes
-- Cliente 1: Estandar
INSERT INTO clientes (id, nombre, tipo_cliente, nit) VALUES (1, 'Juan Perez', 'ESTANDAR', NULL);
-- Cliente 2: VIP
INSERT INTO clientes (id, nombre, tipo_cliente, nit) VALUES (2, 'Maria Gomez', 'VIP', NULL);
-- Cliente 3: Frecuente con > 10 pedidos
INSERT INTO clientes (id, nombre, tipo_cliente, nit) VALUES (3, 'Carlos Lopez', 'FRECUENTE', NULL);
-- Cliente 4: Moroso con deuda pendiente
INSERT INTO clientes (id, nombre, tipo_cliente, nit) VALUES (4, 'Andres Mora', 'MOROSO', NULL);
-- Cliente 5: Corporativo con NIT
INSERT INTO clientes (id, nombre, tipo_cliente, nit) VALUES (5, 'Tech Solutions SAS', 'ESTANDAR', '900123456-1');
-- Cliente 6: Frecuente con 5 pedidos
INSERT INTO clientes (id, nombre, tipo_cliente, nit) VALUES (6, 'Laura Torres', 'FRECUENTE', NULL);

-- Facturas pendientes
INSERT INTO facturas (cliente_id, monto, pagada) VALUES (4, 450000.0, false);
INSERT INTO facturas (cliente_id, monto, pagada) VALUES (1, 100000.0, true);

-- Pedidos previos para cliente 3 (11 pedidos previos -> descuento 8%)
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (3, 100000, 0, 19000, 119000, CURRENT_TIMESTAMP, 'CONFIRMADO');

-- Pedidos previos para cliente 6 (5 pedidos previos -> descuento 4%)
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (6, 50000, 0, 9500, 59500, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (6, 50000, 0, 9500, 59500, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (6, 50000, 0, 9500, 59500, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (6, 50000, 0, 9500, 59500, CURRENT_TIMESTAMP, 'CONFIRMADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (6, 50000, 0, 9500, 59500, CURRENT_TIMESTAMP, 'CONFIRMADO');
