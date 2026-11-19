package com.tienda.pedidos.validacion;

/**
 * Eslabón abstracto de la cadena de responsabilidad (Chain of Responsibility).
 * Cada validador evalúa su condición específica y decide si continuar o cortar el flujo.
 */
public abstract class ValidadorPedido {

    private ValidadorPedido siguiente;

    public ValidadorPedido encadenar(ValidadorPedido siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    public final void validar(ContextoPedido contexto) {
        ejecutar(contexto);
        if (!contexto.isRechazado() && siguiente != null) {
            siguiente.validar(contexto);
        }
    }

    protected abstract void ejecutar(ContextoPedido contexto);
}
