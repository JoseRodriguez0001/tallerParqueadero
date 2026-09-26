package com.parqueadero.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.parqueadero.comun.NegocioException;

class VehiculoTest {

    private final TipoVehiculo carro = TipoVehiculo.reconstruir(2, "CARRO", "Automóvil", true);

    @Test
    @DisplayName("La placa se guarda normalizada: 'abc-123 ' → 'ABC123'")
    void normalizaLaPlaca() {
        Vehiculo vehiculo = new Vehiculo("abc-123 ", carro);

        assertThat(vehiculo.getPlaca()).isEqualTo("ABC123");
        assertThat(vehiculo.getTipoVehiculo()).isSameAs(carro);
        assertThat(vehiculo.getId()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", " - " })
    @DisplayName("Sin placa (null, vacía o solo espacios/guiones) no se crea el vehículo")
    void placaVacia(String placa) {
        assertThatThrownBy(() -> new Vehiculo(placa, carro))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Ingrese la placa del vehículo.");
    }

    @Test
    @DisplayName("Una placa con caracteres distintos de letras y números no es válida")
    void placaConCaracteresInvalidos() {
        assertThatThrownBy(() -> new Vehiculo("AB*123", carro))
                .isInstanceOf(NegocioException.class)
                .hasMessage("La placa AB*123 no es válida: use solo letras y números, máximo 10 caracteres.");
    }

    @Test
    @DisplayName("Una placa de más de 10 caracteres no es válida")
    void placaDemasiadoLarga() {
        assertThatThrownBy(() -> new Vehiculo("ABCDEF123456", carro))
                .isInstanceOf(NegocioException.class)
                .hasMessage("La placa ABCDEF123456 no es válida: use solo letras y números, máximo 10 caracteres.");
    }

    @Test
    @DisplayName("Una placa de exactamente 10 caracteres es válida")
    void placaDeDiezCaracteres() {
        assertThat(new Vehiculo("ABCDE12345", carro).getPlaca()).isEqualTo("ABCDE12345");
    }

    @Test
    @DisplayName("Sin tipo de vehículo no se crea el vehículo")
    void tipoNulo() {
        assertThatThrownBy(() -> new Vehiculo("ABC123", null))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Seleccione el tipo de vehículo.");
    }

    @Test
    @DisplayName("Un tipo de vehículo inactivo no está disponible para registrar")
    void tipoInactivo() {
        TipoVehiculo bici = TipoVehiculo.reconstruir(3, "BICI", "Bicicleta", false);

        assertThatThrownBy(() -> new Vehiculo("ABC123", bici))
                .isInstanceOf(NegocioException.class)
                .hasMessage("El tipo de vehículo Bicicleta no está disponible.");
    }

    @Test
    @DisplayName("reconstruir no valida: el dato ya fue validado al crearse")
    void reconstruirNoValida() {
        TipoVehiculo bici = TipoVehiculo.reconstruir(3, "BICI", "Bicicleta", false);

        Vehiculo vehiculo = Vehiculo.reconstruir(7, "ab*-cualquier cosa", bici);

        assertThat(vehiculo.getId()).isEqualTo(7);
        assertThat(vehiculo.getPlaca()).isEqualTo("ab*-cualquier cosa");
        assertThat(vehiculo.getTipoVehiculo()).isSameAs(bici);
    }
}
