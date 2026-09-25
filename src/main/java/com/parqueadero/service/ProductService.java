package com.parqueadero.service;

import java.util.List;
import java.util.UUID;

import com.parqueadero.service.dto.CreateProductRequest;
import com.parqueadero.service.dto.ProductResponse;

/**
 * Contrato de la capa de servicio.
 * La presentación depende de esta interfaz, no de la implementación concreta.
 */
public interface ProductService {

    /** Crea un nuevo producto. */
    ProductResponse createProduct(CreateProductRequest request);

    /** Retorna un producto por su ID. Lanza ProductNotFoundException si no existe. */
    ProductResponse findById(UUID id);

    /** Retorna todos los productos disponibles. */
    List<ProductResponse> findAll();

    /**
     * Vende una cantidad de un producto, reduciendo su stock.
     * Lanza ProductNotFoundException si el producto no existe.
     * Lanza IllegalStateException si el stock es insuficiente.
     */
    ProductResponse sellProduct(UUID id, int quantity);

    /** Elimina un producto por su ID. */
    void deleteProduct(UUID id);
}
