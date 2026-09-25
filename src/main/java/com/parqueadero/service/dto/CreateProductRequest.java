package com.parqueadero.service.dto;

/**
 * DTO de entrada para crear un producto.
 * Record inmutable — Java 17+.
 *
 * Valida la entrada antes de pasarla al servicio.
 */
public record CreateProductRequest(
        String name,
        double price,
        int stock
) {
    // Validación compacta del record
    public CreateProductRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es requerido.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("El precio debe ser >= 0.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock debe ser >= 0.");
        }
    }
}
