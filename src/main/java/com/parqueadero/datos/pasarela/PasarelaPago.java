package com.parqueadero.datos.pasarela;

import java.math.BigDecimal;

public interface PasarelaPago {
    ResultadoPago procesar(BigDecimal valor, String placa);
}
