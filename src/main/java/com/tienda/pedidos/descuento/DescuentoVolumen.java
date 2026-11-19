package com.tienda.pedidos.descuento;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.validacion.ContextoPedido;
import org.springframework.stereotype.Component;

/**
 * Estrategia de descuento por volumen:
 * Aplica un 12% de descuento si la sumatoria de unidades del pedido supera 20 unidades.
 */
@Component
public class DescuentoVolumen implements EstrategiaDescuento {

    @Override
    public double calcular(ContextoPedido contexto) {
        if (contexto.getRequest().getItems() == null) {
            return 0.0;
        }
        int totalUnidades = contexto.getRequest().getItems().stream()
                .mapToInt(ItemPedido::getCantidad)
                .sum();
        return totalUnidades > 20 ? 0.12 : 0.0;
    }
}
