package com.parqueadero.service.mapper;

import com.parqueadero.domain.Product;
import com.parqueadero.service.dto.ProductResponse;

/**
 * Mapper entre la entidad de dominio y el DTO de respuesta.
 *
 * Centraliza la conversión para que ninguna capa dependa directamente
 * de la estructura interna de la entidad.
 */
public final class ProductMapper {

    // Clase utilitaria — no instanciar
    private ProductMapper() {}

    /** Convierte una entidad de dominio a DTO de respuesta. */
    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }
}
