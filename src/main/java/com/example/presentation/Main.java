package com.example.presentation;

import com.example.repository.ProductRepository;
import com.example.repository.impl.InMemoryProductRepository;
import com.example.service.ProductService;
import com.example.service.impl.ProductServiceImpl;

/**
 * Punto de entrada de la aplicación.
 *
 * Aquí se hace el "wiring" manual de dependencias (Composition Root).
 * En un contexto con Spring, este archivo sería reemplazado por la
 * configuración de beans (@Configuration / @ComponentScan).
 */
public class Main {

    public static void main(String[] args) {

        // ── Composition Root: construir el grafo de dependencias ──────────────
        ProductRepository repository = new InMemoryProductRepository();
        ProductService    service    = new ProductServiceImpl(repository);
        ProductController controller = new ProductController(service);

        // ── Demo de funcionalidades ───────────────────────────────────────────
        System.out.println("=== Layered Architecture App — Demo ===\n");

        // Crear productos
        controller.createProduct("Laptop Pro",   1299.99, 10);
        controller.createProduct("Mouse Inalámbrico", 25.50, 50);
        controller.createProduct("Teclado Mecánico",  89.99, 25);

        // Intentar crear uno duplicado
        controller.createProduct("Laptop Pro", 999.00, 5);

        System.out.println();

        // Listar productos
        controller.listProducts();

        System.out.println();

        // Vender
        // (Para probar, necesitarías capturar el UUID de un producto real;
        //  aquí se muestra con un UUID inválido para ver el manejo de error)
        controller.sellProduct("00000000-0000-0000-0000-000000000000", 3);
    }
}
