package com.parqueadero.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.parqueadero.domain.Product;

/**
 * Contrato del repositorio de Product.
 *
 * La capa de servicio depende SOLO de esta interfaz, nunca de la implementación concreta.
 * Esto permite intercambiar la persistencia (in-memory, SQL, NoSQL) sin tocar el negocio.
 */
public interface ProductRepository {

    /** Persiste un nuevo producto o actualiza uno existente. */
    Product save(Product product);

    /** Busca un producto por su identificador único. */
    Optional<Product> findById(UUID id);

    /** Retorna todos los productos almacenados. */
    List<Product> findAll();

    /** Elimina un producto por su identificador. */
    void deleteById(UUID id);

    /** Verifica si existe un producto con el nombre dado (útil para evitar duplicados). */
    boolean existsByName(String name);
}
