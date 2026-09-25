package com.parqueadero.domain;

import java.util.UUID;

/**
 * Entidad de dominio: Product.
 * Representa el núcleo del negocio — no depende de ninguna capa externa.
 *
 * Adapt this class to your actual domain entity (e.g., Student, Order, etc.)
 */
public class Product {

    private final UUID id;
    private String name;
    private double price;
    private int stock;

    // Constructor privado: usar la factory method para creación
    private Product(UUID id, String name, double price, int stock) {
        this.id    = id;
        this.name  = name;
        this.price = price;
        this.stock = stock;
    }

    /** Factory method para crear una nueva instancia con ID auto-generado. */
    public static Product create(String name, double price, int stock) {
        validateName(name);
        validatePrice(price);
        validateStock(stock);
        return new Product(UUID.randomUUID(), name, price, stock);
    }

    /** Factory method para reconstruir desde persistencia (con ID existente). */
    public static Product reconstitute(UUID id, String name, double price, int stock) {
        validateName(name);
        validatePrice(price);
        validateStock(stock);
        return new Product(id, name, price, stock);
    }

    // ── Comportamiento de dominio ──────────────────────────────────────────────

    /**
     * Reduce el stock al vender unidades.
     * @throws IllegalArgumentException si la cantidad supera el stock disponible.
     */
    public void sell(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad a vender debe ser mayor a cero.");
        }
        if (quantity > this.stock) {
            throw new IllegalStateException(
                    String.format("Stock insuficiente. Disponible: %d, solicitado: %d", this.stock, quantity));
        }
        this.stock -= quantity;
    }

    /** Actualiza el precio del producto. */
    public void updatePrice(double newPrice) {
        validatePrice(newPrice);
        this.price = newPrice;
    }

    // ── Validaciones privadas de dominio ──────────────────────────────────────

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }
    }

    private static void validatePrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
    }

    private static void validateStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
    }

    // ── Getters (sin setters: inmutabilidad controlada por comportamiento) ────

    public UUID getId()     { return id; }
    public String getName() { return name; }
    public double getPrice(){ return price; }
    public int getStock()   { return stock; }

    @Override
    public String toString() {
        return String.format("Product{id=%s, name='%s', price=%.2f, stock=%d}",
                id, name, price, stock);
    }
}
