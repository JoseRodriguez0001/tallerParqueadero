package com.example.service.dto;

import java.util.UUID;

/**
 * DTO de salida: representa lo que la capa de presentación recibe.
 * Nunca expone la entidad de dominio directamente.
 * Record inmutable — Java 17+.
 */
public record ProductResponse(
        UUID id,
        String name,
        double price,
        int stock
) {}
