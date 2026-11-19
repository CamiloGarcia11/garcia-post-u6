package com.tienda.pedidos.validacion;

import com.tienda.pedidos.dto.ItemPedido;
import org.springframework.stereotype.Component;

/**
 * Nuevo eslabón: volumen -- necesita el subtotal de items, que el contexto todavía
 * no expone en unidades, así que recalcula la cantidad total él mismo desde el request.
 */
@Component
public class PromocionVolumen extends ValidadorPedido {

    @Override
    protected void ejecutar(ContextoPedido contexto) {
        if (contexto.getRequest().getItems() == null) {
            return;
        }
        int totalUnidades = contexto.getRequest().getItems().stream()
                .mapToInt(ItemPedido::getCantidad)
                .sum();
        if (totalUnidades > 20) {
            contexto.aplicarDescuentoCampana(0.12);
        }
    }
}
