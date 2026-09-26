package com.parqueadero.presentacion.vista;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.GraphicsEnvironment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parqueadero.negocio.dto.EstadiaDTO;
import com.parqueadero.negocio.dto.TipoVehiculoDTO;
import com.parqueadero.negocio.dto.VehiculoDTO;

// Prueba de humo: los paneles se construyen y aceptan datos sin errores (no abre ventanas).
// Se omite en entornos sin interfaz gráfica.
class PanelesTest {

    private static final LocalDateTime INGRESO = LocalDateTime.of(2026, 9, 26, 8, 0);
    private final EstadiaDTO pendiente = new EstadiaDTO("ABC123", "Automóvil", INGRESO, INGRESO.plusMinutes(160),
            new BigDecimal("5400"), "PENDIENTE_PAGO");

    @BeforeAll
    static void requiereInterfazGrafica() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Entorno sin interfaz gráfica");
    }

    @Test
    @DisplayName("Sin selección, el selector de tipo devuelve null y la placa llega vacía")
    void panelIngresoSinDatos() {
        PanelIngreso panel = new PanelIngreso();
        panel.setTiposVehiculo(List.of(new TipoVehiculoDTO("CARRO", "Automóvil")));

        assertThat(panel.getPlaca()).isEmpty();
        assertThat(panel.getCodigoTipo()).isNull();
    }

    @Test
    @DisplayName("Los paneles muestran resultados y listas sin lanzar errores")
    void panelesMuestranDatos() {
        new PanelIngreso().mostrarIngreso(pendiente);
        new PanelSalida().mostrarLiquidacion(pendiente);

        PanelPagoCaja pago = new PanelPagoCaja();
        pago.mostrarValor(pendiente);
        pago.mostrarPagoRegistrado(pendiente);

        PanelVehiculos vehiculos = new PanelVehiculos();
        vehiculos.mostrarEnParqueadero(List.of(pendiente));
        vehiculos.mostrarRegistrados(List.of(new VehiculoDTO("ABC123", "Automóvil", true)));

        assertThat(new VentanaPrincipal(vehiculos, new PanelIngreso(), new PanelSalida(), pago).getTitle())
                .contains("Parqueadero");
    }
}
