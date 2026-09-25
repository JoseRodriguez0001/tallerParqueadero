package com.parqueadero.service.impl;

import java.util.List;
import java.util.UUID;

import com.parqueadero.domain.Product;
import com.parqueadero.domain.exception.ProductNotFoundException;
import com.parqueadero.repository.ProductRepository;
import com.parqueadero.service.ProductService;
import com.parqueadero.service.dto.CreateProductRequest;
import com.parqueadero.service.dto.ProductResponse;
import com.parqueadero.service.mapper.ProductMapper;

/**
 * Implementación de ProductService.
 *
 * Contiene la lógica de negocio orquestando el dominio y el repositorio.
 * Depende únicamente de interfaces (ProductRepository, ProductService).
 */
public class ProductServiceImpl implements ProductService {

    // Inyección por constructor — nunca por campo directamente
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByName(request.name())) {
            throw new IllegalArgumentException(
                    String.format("Ya existe un producto con el nombre '%s'.", request.name()));
        }

        Product product = Product.create(request.name(), request.price(), request.stock());
        Product saved = productRepository.save(product);

        return ProductMapper.toResponse(saved);
    }

    @Override
    public ProductResponse findById(UUID id) {
        Product product = findProductOrThrow(id);
        return ProductMapper.toResponse(product);
    }

    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Override
    public ProductResponse sellProduct(UUID id, int quantity) {
        Product product = findProductOrThrow(id);
        product.sell(quantity);                // comportamiento en el dominio
        Product updated = productRepository.save(product);
        return ProductMapper.toResponse(updated);
    }

    @Override
    public void deleteProduct(UUID id) {
        findProductOrThrow(id);               // verifica existencia antes de borrar
        productRepository.deleteById(id);
    }

    // ── Helpers privados ───────────────────────────────────────────────────────

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id.toString()));
    }
}
