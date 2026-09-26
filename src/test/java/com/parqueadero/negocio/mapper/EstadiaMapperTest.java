package com.parqueadero.negocio.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parqueadero.dominio.modelo.EstadoEstadia;
import com.parqueadero.dominio.modelo.Estadia;
import com.parqueadero.dominio.modelo.Tarifa;
import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.dominio.modelo.Vehiculo;
import com.parqueadero.negocio.dto.EstadiaDTO;

class EstadiaMapperTest {

    private static final LocalDateTime INGRESO = LocalDateTime.of(2026, 9, 26, 8, 0);
    private static final LocalDateTime SALIDA = LocalDateTime.of(2026, 9, 26, 10, 40);

    private final TipoVehiculo carro = TipoVehiculo.reconstruir(2, "CARRO", "Automóvil", true);
    private final Vehiculo vehiculo = Vehiculo.reconstruir(10, "ABC123", carro);
    private final Tarifa tarifa = Tarifa.reconstruir(20, carro, new BigDecimal("1800"), INGRESO.minusYears(1), null);

    @Test
    @DisplayName("Copia placa, nombre del tipo, fechas, estado y el valor recibido")
    void estadiaPendiente() {
        Estadia estadia = Estadia.reconstruir(1, vehiculo, tarifa, INGRESO, SALIDA,
                new BigDecimal("5400"), EstadoEstadia.PENDIENTE_PAGO, null);

        EstadiaDTO dto = EstadiaMapper.aDTO(estadia, new BigDecimal("5400"));

        assertThat(dto.placa()).isEqualTo("ABC123");
        assertThat(dto.tipo()).isEqualTo("Automóvil");
        assertThat(dto.fechaIngreso()).isEqualTo(INGRESO);
        assertThat(dto.fechaSalida()).isEqualTo(SALIDA);
        assertThat(dto.valor()).isEqualByComparingTo("5400");
        assertThat(dto.estado()).isEqualTo("PENDIENTE_PAGO");
    }

    @Test
    @DisplayName("Una estadía DENTRO se convierte con fecha de salida null (el DTO no usa Optional)")
    void estadiaDentro() {
        Estadia estadia = Estadia.reconstruir(1, vehiculo, tarifa, INGRESO, null, null, EstadoEstadia.DENTRO, null);

        EstadiaDTO dto = EstadiaMapper.aDTO(estadia, new BigDecimal("1800"));

        assertThat(dto.fechaSalida()).isNull();
        assertThat(dto.valor()).isEqualByComparingTo("1800");
        assertThat(dto.estado()).isEqualTo("DENTRO");
    }
}
