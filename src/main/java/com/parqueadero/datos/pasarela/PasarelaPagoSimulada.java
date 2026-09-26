package com.parqueadero.datos.pasarela;

import java.math.BigDecimal;
import java.util.UUID;

// Simula la pasarela externa: confirma siempre y genera una referencia única.
// Integrar una pasarela real solo requiere otra implementación de PasarelaPago conectada en Aplicacion.
public class PasarelaPagoSimulada implements PasarelaPago {

    @Override
    public ResultadoPago procesar(BigDecimal valor, String placa) {
        String referencia = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new ResultadoPago(true, referencia);
    }
}
