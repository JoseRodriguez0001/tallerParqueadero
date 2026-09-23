package com.example.presentation;

import com.example.domain.exception.ProductNotFoundException;
import com.example.service.ProductService;
import com.example.service.dto.CreateProductRequest;
import com.example.service.dto.ProductResponse;

import java.util.List;
import java.util.UUID;

/**
 * Controlador de la capa de presentación (consola / CLI).
 *
 * Para un contexto web, esta clase sería reemplazada por un Controller HTTP.
 * Depende únicamente de la interfaz ProductService, nunca de la implementación.
 */
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** Solicita la creación de un producto. */
    public void createProduct(String name, double price, int stock) {
        try {
            CreateProductRequest request = new CreateProductRequest(name, price, stock);
            ProductResponse response = productService.createProduct(request);
            System.out.println("[OK] Producto creado: " + format(response));
        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }

    /** Lista todos los productos. */
    public void listProducts() {
        List<ProductResponse> products = productService.findAll();
        if (products.isEmpty()) {
            System.out.println("[INFO] No hay productos registrados.");
            return;
        }
        System.out.println("[INFO] Productos disponibles:");
        products.forEach(p -> System.out.println("  - " + format(p)));
    }

    /** Vende unidades de un producto. */
    public void sellProduct(String rawId, int quantity) {
        try {
            UUID id = UUID.fromString(rawId);
            ProductResponse response = productService.sellProduct(id, quantity);
            System.out.println("[OK] Venta registrada. Stock actualizado: " + format(response));
        } catch (ProductNotFoundException e) {
            System.err.println("[NOT FOUND] " + e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }

    /** Elimina un producto por ID. */
    public void deleteProduct(String rawId) {
        try {
            UUID id = UUID.fromString(rawId);
            productService.deleteProduct(id);
            System.out.println("[OK] Producto eliminado.");
        } catch (ProductNotFoundException e) {
            System.err.println("[NOT FOUND] " + e.getMessage());
        }
    }

    // ── Helpers de formato ────────────────────────────────────────────────────

    private String format(ProductResponse p) {
        return String.format("id=%s | name='%s' | price=%.2f | stock=%d",
                p.id(), p.name(), p.price(), p.stock());
    }
}
