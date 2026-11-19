package com.tienda.pedidos.descuento;

import com.tienda.pedidos.validacion.ContextoPedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Estrategia de descuento para la campaña Black Friday:
 * Aplica un 25% fijo si la campaña está habilitada en la configuración.
 */
@Component
public class DescuentoBlackFriday implements EstrategiaDescuento {

    private final boolean campanaActiva;

    public DescuentoBlackFriday(@Value("${promo.black-friday.activa:true}") boolean campanaActiva) {
        this.campanaActiva = campanaActiva;
    }

    @Override
    public double calcular(ContextoPedido contexto) {
        return campanaActiva ? 0.25 : 0.0;
    }
}
