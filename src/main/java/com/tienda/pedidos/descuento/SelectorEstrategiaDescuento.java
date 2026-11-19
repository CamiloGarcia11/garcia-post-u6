package com.tienda.pedidos.descuento;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Selector de estrategias de descuento (Strategy + Simple Factory):
 * Asocia dinámicamente el tipo de cliente con su respectiva estrategia,
 * eliminando bifurcaciones condicionales y respetando OCP.
 */
@Component
public class SelectorEstrategiaDescuento {

    private final Map<String, EstrategiaDescuento> estrategias;

    public SelectorEstrategiaDescuento(DescuentoVip vip, DescuentoFrecuente frecuente, DescuentoEstandar estandar) {
        this.estrategias = Map.of(
            "VIP", vip,
            "FRECUENTE", frecuente,
            "ESTANDAR", estandar
        );
    }

    public EstrategiaDescuento seleccionar(String tipoCliente) {
        if (tipoCliente == null) {
            return estrategias.get("ESTANDAR");
        }
        return estrategias.getOrDefault(tipoCliente, estrategias.get("ESTANDAR"));
    }
}
