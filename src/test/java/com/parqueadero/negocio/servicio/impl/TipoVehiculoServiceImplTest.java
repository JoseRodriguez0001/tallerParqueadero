package com.parqueadero.negocio.servicio.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parqueadero.datos.repositorio.TipoVehiculoRepository;
import com.parqueadero.datos.transaccion.GestorTransacciones;
import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.negocio.dto.TipoVehiculoDTO;

class TipoVehiculoServiceImplTest {

    private GestorFalso gestor;
    private TipoVehiculoRepository tipos;
    private TipoVehiculoServiceImpl servicio;

    @BeforeEach
    void setUp() {
        gestor = new GestorFalso();
        tipos = mock(TipoVehiculoRepository.class);
        servicio = new TipoVehiculoServiceImpl(gestor, tipos);
    }

    @Test
    @DisplayName("Convierte cada tipo activo en un DTO con su código y nombre, en una transacción")
    void listaLosTiposActivos() {
        when(tipos.listarActivos()).thenReturn(List.of(
                TipoVehiculo.reconstruir(2, "CARRO", "Automóvil", true),
                TipoVehiculo.reconstruir(1, "MOTO", "Motocicleta", true)));

        List<TipoVehiculoDTO> lista = servicio.listarActivos();

        assertThat(lista).containsExactly(
                new TipoVehiculoDTO("CARRO", "Automóvil"),
                new TipoVehiculoDTO("MOTO", "Motocicleta"));
        assertThat(gestor.transacciones).isEqualTo(1);
    }

    @Test
    @DisplayName("Sin tipos activos devuelve una lista vacía")
    void sinTipos() {
        when(tipos.listarActivos()).thenReturn(List.of());

        assertThat(servicio.listarActivos()).isEmpty();
    }

    // Ejecuta la operación directamente y cuenta cuántas transacciones se abrieron
    private static final class GestorFalso implements GestorTransacciones {
        int transacciones;

        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            transacciones++;
            return operacion.get();
        }
    }
}
