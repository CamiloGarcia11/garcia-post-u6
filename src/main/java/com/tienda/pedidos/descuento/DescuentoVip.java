package com.tienda.pedidos.descuento;

import com.tienda.pedidos.validacion.ContextoPedido;
import org.springframework.stereotype.Component;

/**
 * Estrategia de descuento para clientes VIP:
 * - Subtotal > 1_000_000: 15%
 * - Subtotal > 500_000: 10%
 * - En otro caso: 5%
 */
@Component
public class DescuentoVip implements EstrategiaDescuento {

    @Override
    public double calcular(ContextoPedido contexto) {
        double subtotal = contexto.getSubtotal();
        if (subtotal > 1_000_000) return 0.15;
        if (subtotal > 500_000) return 0.10;
        return 0.05;
    }
}
