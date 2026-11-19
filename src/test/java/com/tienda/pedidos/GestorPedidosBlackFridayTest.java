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
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "promo.black-friday.activa=true")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GestorPedidosBlackFridayTest {

    @Autowired
    private GestorPedidos gestorPedidos;

    @Test
    @DisplayName("Campaña 3: Descuento Black Friday activo (25% fijo)")
    void testCampanaBlackFridayActiva() {
        // Cliente 1 estándar (0% base). Campaña Black Friday activa da 25%.
        // Producto 1 = 100_000. Subtotal = 100_000. Descuento 25% = 25_000.
        // Base gravable = 75_000. IVA (19%) = 14_250. Total = 89_250.
        PedidoRequest request = new PedidoRequest(1L, "blackfriday@mail.com",
                List.of(new ItemPedido(1L, 1)));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertTrue(resultado.isConfirmado());
        assertNotNull(resultado.getPedidoId());
        assertEquals(89250.0, resultado.getTotal(), 0.01);
    }
}
