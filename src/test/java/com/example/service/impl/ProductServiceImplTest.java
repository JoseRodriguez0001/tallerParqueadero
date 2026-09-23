package com.example.service.impl;

import com.example.domain.exception.ProductNotFoundException;
import com.example.repository.ProductRepository;
import com.example.service.dto.CreateProductRequest;
import com.example.service.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.domain.Product;

/**
 * Tests unitarios de ProductServiceImpl.
 * Convención de nombre: dado_[contexto]_cuando_[acción]_entonces_[resultado]
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.create("Laptop Pro", 1299.99, 10);
    }

    // ── createProduct ─────────────────────────────────────────────────────────

    @Test
    void dado_nombreUnico_cuando_createProduct_entonces_retornaProductoCreado() {
        when(productRepository.existsByName("Laptop Pro")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(
                new CreateProductRequest("Laptop Pro", 1299.99, 10));

        assertThat(response.name()).isEqualTo("Laptop Pro");
        assertThat(response.price()).isEqualTo(1299.99);
        assertThat(response.stock()).isEqualTo(10);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void dado_nombreDuplicado_cuando_createProduct_entonces_lanzaIllegalArgumentException() {
        when(productRepository.existsByName("Laptop Pro")).thenReturn(true);

        assertThatThrownBy(() ->
                productService.createProduct(new CreateProductRequest("Laptop Pro", 999.0, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Laptop Pro");

        verify(productRepository, never()).save(any());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void dado_idExistente_cuando_findById_entonces_retornaProducto() {
        when(productRepository.findById(sampleProduct.getId()))
                .thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.findById(sampleProduct.getId());

        assertThat(response.id()).isEqualTo(sampleProduct.getId());
    }

    @Test
    void dado_idInexistente_cuando_findById_entonces_lanzaProductNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(productRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(unknownId))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void dado_productosExistentes_cuando_findAll_entonces_retornaLista() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        List<ProductResponse> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Laptop Pro");
    }

    // ── sellProduct ───────────────────────────────────────────────────────────

    @Test
    void dado_stockSuficiente_cuando_sellProduct_entonces_actualizaStock() {
        when(productRepository.findById(sampleProduct.getId()))
                .thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.sellProduct(sampleProduct.getId(), 3);

        assertThat(response.stock()).isEqualTo(7); // 10 - 3
    }

    @Test
    void dado_stockInsuficiente_cuando_sellProduct_entonces_lanzaIllegalStateException() {
        when(productRepository.findById(sampleProduct.getId()))
                .thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> productService.sellProduct(sampleProduct.getId(), 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");
    }
}
