package com.example.repository.impl;

import com.example.domain.Product;
import com.example.repository.ProductRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación in-memory de ProductRepository.
 *
 * Útil para desarrollo, pruebas o demo sin necesidad de una base de datos real.
 * Para producción, reemplazar por una implementación JDBC/JPA/SQL sin modificar
 * ninguna otra clase (principio Open/Closed).
 */
public class InMemoryProductRepository implements ProductRepository {

    // Simula la base de datos con un mapa thread-safe
    private final Map<UUID, Product> store = new ConcurrentHashMap<>();

    @Override
    public Product save(Product product) {
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }

    @Override
    public boolean existsByName(String name) {
        return store.values().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
    }
}
