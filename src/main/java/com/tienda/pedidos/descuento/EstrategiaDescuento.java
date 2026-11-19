package com.tienda.pedidos.descuento;

import com.tienda.pedidos.validacion.ContextoPedido;

/**
 * Patrón Strategy: Interfaz para el cálculo de descuentos comerciales.
 * Cada política encapsula su propio algoritmo sin depender de un orden procedural.
 */
public interface EstrategiaDescuento {
    double calcular(ContextoPedido contexto);
}
