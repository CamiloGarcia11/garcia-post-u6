package com.tienda.pedidos;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.dto.PedidoRequest;
import com.tienda.pedidos.dto.ResultadoPedido;
import com.tienda.pedidos.service.GestorPedidos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GestorPedidosTest {

    @Autowired
    private GestorPedidos gestorPedidos;

    @Test
    @DisplayName("Ruta 1: Rechazo por stock insuficiente (ValidadorStock)")
    void testStockInsuficiente() {
        // Producto 2 tiene stock de 5 unidades. Solicitamos 10.
        PedidoRequest request = new PedidoRequest(1L, "cliente1@mail.com",
                List.of(new ItemPedido(2L, 10)));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertFalse(resultado.isConfirmado());
        assertNotNull(resultado.getMotivoRechazo());
        assertTrue(resultado.getMotivoRechazo().contains("Stock insuficiente"));
    }

    @Test
    @DisplayName("Ruta 2: Rechazo por cliente inexistente (ValidadorCliente)")
    void testClienteInexistente() {
        PedidoRequest request = new PedidoRequest(999L, "desconocido@mail.com",
                List.of(new ItemPedido(1L, 1)));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertFalse(resultado.isConfirmado());
        assertEquals("Cliente no registrado", resultado.getMotivoRechazo());
    }

    @Test
    @DisplayName("Ruta 3: Validación cliente moroso según horario de corte 20:00 (ValidadorCliente)")
    void testClienteMoroso() {
        PedidoRequest request = new PedidoRequest(4L, "moroso@mail.com",
                List.of(new ItemPedido(1L, 1)));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);
        LocalTime ahora = LocalTime.now();

        if (ahora.isBefore(LocalTime.of(20, 0))) {
            assertFalse(resultado.isConfirmado());
            assertTrue(resultado.getMotivoRechazo().contains("Cliente con deuda pendiente"));
        } else {
            // Fuera de horario de corte (excepcionalmente permitido)
            assertTrue(resultado.isConfirmado());
            assertNotNull(resultado.getPedidoId());
        }
    }

    @Test
    @DisplayName("Ruta 4: Descuento VIP escalonado (> 1_000_000 -> 15%)")
    void testDescuentoVip() {
        // Producto 3 vale 1_200_000. Subtotal = 1_200_000. Descuento = 15% (180_000).
        // Base gravable = 1_020_000. IVA (19%) = 193_800. Total = 1_213_800.
        PedidoRequest request = new PedidoRequest(2L, "vip@mail.com",
                List.of(new ItemPedido(3L, 1)));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertTrue(resultado.isConfirmado());
        assertNotNull(resultado.getPedidoId());
        assertEquals(1213800.0, resultado.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Ruta 5: Descuento FRECUENTE (> 10 pedidos previos -> 8%)")
    void testDescuentoFrecuente() {
        // Cliente 3 tiene 11 pedidos previos en BD. Descuento = 8%.
        // Producto 1 vale 100_000. Subtotal = 100_000. Descuento = 8% (8_000).
        // Base gravable = 92_000. IVA (19%) = 17_480. Total = 109_480.
        PedidoRequest request = new PedidoRequest(3L, "frecuente@mail.com",
                List.of(new ItemPedido(1L, 1)));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertTrue(resultado.isConfirmado());
        assertNotNull(resultado.getPedidoId());
        assertEquals(109480.0, resultado.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Campaña 1: Descuento Corporativo (Cliente con NIT -> 10%)")
    void testCampanaCorporativo() {
        // Cliente 5 tiene NIT = '900123456-1'. Descuento = 10%.
        // Producto 1 vale 100_000. Subtotal = 100_000. Descuento = 10% (10_000).
        // Base gravable = 90_000. IVA (19%) = 17_100. Total = 107_100.
        PedidoRequest request = new PedidoRequest(5L, "corporativo@empresa.com",
                List.of(new ItemPedido(1L, 1)));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertTrue(resultado.isConfirmado());
        assertNotNull(resultado.getPedidoId());
        assertEquals(107100.0, resultado.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Campaña 2: Descuento por Volumen (> 20 unidades -> 12%)")
    void testCampanaVolumen() {
        // Cliente 1 estándar (0% base). Pedido con 25 unidades de Producto 4 (250_000 c/u).
        // Subtotal = 25 * 250_000 = 6_250_000.
        // Unidades = 25 (> 20) -> Descuento = 12% (750_000).
        // Base gravable = 5_500_000. IVA (19%) = 1_045_000. Total = 6_545_000.
        PedidoRequest request = new PedidoRequest(1L, "volumen@mail.com",
                List.of(new ItemPedido(4L, 25)));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertTrue(resultado.isConfirmado());
        assertNotNull(resultado.getPedidoId());
        assertEquals(6545000.0, resultado.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Regla de negocio: El mayor descuento gana (VIP 15% vs Volumen 12% -> VIP 15%)")
    void testMayorDescuentoGana() {
        // Cliente 2 VIP con subtotal > 1M (15% por tipo cliente).
        // Pedido con 25 unidades de Producto 1 (100_000 c/u) = Subtotal 2_500_000.
        // Unidades = 25 -> Campaña volumen da 12%.
        // Mayor descuento = 15% (375_000). Base = 2_125_000. IVA (19%) = 403_750. Total = 2_528_750.
        PedidoRequest request = new PedidoRequest(2L, "vip.volumen@mail.com",
                List.of(new ItemPedido(1L, 25)));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertTrue(resultado.isConfirmado());
        assertEquals(2528750.0, resultado.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Ruta de guardia: Rechazo por pedido sin items")
    void testSinItems() {
        PedidoRequest request = new PedidoRequest(1L, "vacio@mail.com", List.of());

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertFalse(resultado.isConfirmado());
        assertEquals("El pedido no contiene items", resultado.getMotivoRechazo());
    }
}
