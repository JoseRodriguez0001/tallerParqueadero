package com.example.domain.exception;

/**
 * Excepción de dominio: lanzada cuando un Product no es encontrado.
 * Mapeada a HTTP 404 si se usa en un contexto web.
 */
public class ProductNotFoundException extends RuntimeException {

    private final String productId;

    public ProductNotFoundException(String productId) {
        super(String.format("Producto con id '%s' no encontrado.", productId));
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
