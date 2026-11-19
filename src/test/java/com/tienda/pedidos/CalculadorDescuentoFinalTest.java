package com.tienda.pedidos;

import com.tienda.pedidos.descuento.*;
import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.dto.PedidoRequest;
import com.tienda.pedidos.validacion.ContextoPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CalculadorDescuentoFinalTest {

    private CalculadorDescuentoFinal calculador;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);

        DescuentoVip vip = new DescuentoVip();
        DescuentoFrecuente frecuente = new DescuentoFrecuente(jdbcTemplate);
        DescuentoEstandar estandar = new DescuentoEstandar();
        SelectorEstrategiaDescuento selector = new SelectorEstrategiaDescuento(vip, frecuente, estandar);

        DescuentoBlackFriday blackFriday = new DescuentoBlackFriday(true);
        DescuentoCorporativo corporativo = new DescuentoCorporativo(jdbcTemplate);
        DescuentoVolumen volumen = new DescuentoVolumen();

        calculador = new CalculadorDescuentoFinal(selector, blackFriday, corporativo, volumen);
    }

    @Test
    @DisplayName("Unitario: Black Friday activo (25%) supera cliente estándar (0%)")
    void testBlackFridaySuperaEstandar() {
        PedidoRequest request = new PedidoRequest(1L, "test@mail.com", List.of(new ItemPedido(1L, 1)));
        ContextoPedido contexto = new ContextoPedido(request);
        contexto.setTipoCliente("ESTANDAR");
        contexto.setSubtotal(100_000);

        double descuento = calculador.calcular(contexto);

        assertEquals(0.25, descuento, 0.001);
    }

    @Test
    @DisplayName("Unitario: Corporativo con NIT (10%) cuando Black Friday está inactivo")
    void testCorporativoConNit() {
        DescuentoBlackFriday bfInactivo = new DescuentoBlackFriday(false);
        DescuentoCorporativo corporativo = new DescuentoCorporativo(jdbcTemplate);
        DescuentoVolumen volumen = new DescuentoVolumen();
        DescuentoVip vip = new DescuentoVip();
        DescuentoFrecuente frecuente = new DescuentoFrecuente(jdbcTemplate);
        DescuentoEstandar estandar = new DescuentoEstandar();
        SelectorEstrategiaDescuento selector = new SelectorEstrategiaDescuento(vip, frecuente, estandar);

        when(jdbcTemplate.queryForObject(eq("SELECT nit FROM clientes WHERE id = ?"), eq(String.class), eq(5L)))
                .thenReturn("900123456-1");

        CalculadorDescuentoFinal calc = new CalculadorDescuentoFinal(selector, bfInactivo, corporativo, volumen);

        PedidoRequest request = new PedidoRequest(5L, "corp@test.com", List.of(new ItemPedido(1L, 1)));
        ContextoPedido contexto = new ContextoPedido(request);
        contexto.setTipoCliente("ESTANDAR");
        contexto.setSubtotal(100_000);

        double descuento = calc.calcular(contexto);

        assertEquals(0.10, descuento, 0.001);
    }
}
