-- =====================================================================
-- Sistema de Gestión de Parqueadero - PostgreSQL
-- =====================================================================

DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS estadia;
DROP TABLE IF EXISTS vehiculo;
DROP TABLE IF EXISTS tarifa;
DROP TABLE IF EXISTS tipo_vehiculo;
DROP TABLE IF EXISTS usuario;


-- ---------------------------------------------------------------------
-- Tipos de vehículo
-- ---------------------------------------------------------------------
CREATE TABLE tipo_vehiculo (
    id      INT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo  VARCHAR(20)  NOT NULL,
    nombre  VARCHAR(50)  NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_tipo_vehiculo_codigo UNIQUE (codigo),
    CONSTRAINT uq_tipo_vehiculo_nombre UNIQUE (nombre),
    CONSTRAINT ck_tipo_vehiculo_codigo_mayus CHECK (codigo = UPPER(codigo))
);


-- ---------------------------------------------------------------------
-- Tarifas (vigente_hasta NULL = tarifa vigente)
-- ---------------------------------------------------------------------
CREATE TABLE tarifa (
    id                INT            GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo_vehiculo_id  INT            NOT NULL,
    valor_hora        NUMERIC(10,2)  NOT NULL,
    vigente_desde     TIMESTAMP      NOT NULL,
    vigente_hasta     TIMESTAMP,

    CONSTRAINT fk_tarifa_tipo_vehiculo
        FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo (id),
    CONSTRAINT ck_tarifa_valor_positivo CHECK (valor_hora > 0),
    CONSTRAINT ck_tarifa_vigencia CHECK (vigente_hasta IS NULL OR vigente_hasta > vigente_desde)
);

-- Una sola tarifa vigente por tipo
CREATE UNIQUE INDEX ux_tarifa_vigente_por_tipo
    ON tarifa (tipo_vehiculo_id)
    WHERE vigente_hasta IS NULL;

CREATE INDEX ix_tarifa_tipo_vigencia
    ON tarifa (tipo_vehiculo_id, vigente_desde);


-- ---------------------------------------------------------------------
-- Vehículos
-- ---------------------------------------------------------------------
CREATE TABLE vehiculo (
    id                INT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    placa             VARCHAR(10)  NOT NULL,
    tipo_vehiculo_id  INT          NOT NULL,

    CONSTRAINT uq_vehiculo_placa UNIQUE (placa),
    CONSTRAINT fk_vehiculo_tipo_vehiculo
        FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo (id),
    CONSTRAINT ck_vehiculo_placa_formato CHECK (placa = UPPER(placa) AND placa !~ '\s')
);


-- ---------------------------------------------------------------------
-- Usuarios
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id               INT           GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_usuario   VARCHAR(50)   NOT NULL,
    nombre           VARCHAR(100)  NOT NULL,
    contrasena_hash  VARCHAR(255)  NOT NULL,
    rol              VARCHAR(20)   NOT NULL,
    activo           BOOLEAN       NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_usuario_nombre_usuario UNIQUE (nombre_usuario),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('PERSONAL', 'ADMINISTRADOR'))
);


-- ---------------------------------------------------------------------
-- Estadías (DENTRO -> PENDIENTE_PAGO -> CERRADA)
-- ---------------------------------------------------------------------
CREATE TABLE estadia (
    id             INT            GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehiculo_id    INT            NOT NULL,
    tarifa_id      INT            NOT NULL,
    fecha_ingreso  TIMESTAMP      NOT NULL,
    fecha_salida   TIMESTAMP,
    valor_total    NUMERIC(10,2),
    estado         VARCHAR(20)    NOT NULL DEFAULT 'DENTRO',

    CONSTRAINT fk_estadia_vehiculo
        FOREIGN KEY (vehiculo_id) REFERENCES vehiculo (id),
    CONSTRAINT fk_estadia_tarifa
        FOREIGN KEY (tarifa_id) REFERENCES tarifa (id),
    CONSTRAINT ck_estadia_estado
        CHECK (estado IN ('DENTRO', 'PENDIENTE_PAGO', 'CERRADA')),
    CONSTRAINT ck_estadia_fechas
        CHECK (fecha_salida IS NULL OR fecha_salida >= fecha_ingreso),
    CONSTRAINT ck_estadia_valor_positivo
        CHECK (valor_total IS NULL OR valor_total > 0),
    CONSTRAINT ck_estadia_coherencia_estado CHECK (
        (estado = 'DENTRO'
            AND fecha_salida IS NULL AND valor_total IS NULL)
        OR
        (estado IN ('PENDIENTE_PAGO', 'CERRADA')
            AND fecha_salida IS NOT NULL AND valor_total IS NOT NULL)
    )
);

-- Una sola estadía activa por vehículo
CREATE UNIQUE INDEX ux_estadia_activa_por_vehiculo
    ON estadia (vehiculo_id)
    WHERE estado <> 'CERRADA';

CREATE INDEX ix_estadia_estado
    ON estadia (estado);


-- ---------------------------------------------------------------------
-- Pagos
-- ---------------------------------------------------------------------
CREATE TABLE pago (
    id          INT            GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    estadia_id  INT            NOT NULL,
    valor       NUMERIC(10,2)  NOT NULL,
    fecha_pago  TIMESTAMP      NOT NULL,
    canal       VARCHAR(20)    NOT NULL,
    referencia  VARCHAR(100),
    usuario_id  INT,

    CONSTRAINT uq_pago_estadia UNIQUE (estadia_id),
    CONSTRAINT uq_pago_referencia UNIQUE (referencia),
    CONSTRAINT fk_pago_estadia
        FOREIGN KEY (estadia_id) REFERENCES estadia (id),
    CONSTRAINT fk_pago_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_pago_valor_positivo CHECK (valor > 0),
    CONSTRAINT ck_pago_canal CHECK (canal IN ('CAJA', 'EN_LINEA')),
    -- Caja con empleado; en línea con referencia
    CONSTRAINT ck_pago_datos_por_canal CHECK (
        (canal = 'CAJA'
            AND usuario_id IS NOT NULL AND referencia IS NULL)
        OR
        (canal = 'EN_LINEA'
            AND usuario_id IS NULL AND referencia IS NOT NULL)
    )
);

CREATE INDEX ix_pago_fecha
    ON pago (fecha_pago);


-- ---------------------------------------------------------------------
-- Datos iniciales
-- ---------------------------------------------------------------------
INSERT INTO tipo_vehiculo (codigo, nombre) VALUES
    ('MOTO',  'Motocicleta'),
    ('CARRO', 'Automóvil');

INSERT INTO tarifa (tipo_vehiculo_id, valor_hora, vigente_desde) VALUES
    ((SELECT id FROM tipo_vehiculo WHERE codigo = 'MOTO'),   800.00, '2026-01-01 00:00:00'),
    ((SELECT id FROM tipo_vehiculo WHERE codigo = 'CARRO'), 1800.00, '2026-01-01 00:00:00');

-- Usuarios de demostración (contraseñas: admin123 / personal123, SHA-256)
INSERT INTO usuario (nombre_usuario, nombre, contrasena_hash, rol) VALUES
    ('admin',    'Administrador del parqueadero',
     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR'),
    ('personal', 'Empleado del parqueadero',
     '7b75908857ee42291be1b2dc6976c729da6995355227587c3807dde457a700ad', 'PERSONAL');


-- ---------------------------------------------------------------------
-- Demostración de extensibilidad (ejecutar manualmente)
-- Tipos de vehículo y tarifas son datos: cambiarlos no requiere tocar el código.
-- ---------------------------------------------------------------------
/*
-- Nuevo tipo de vehículo
BEGIN;
INSERT INTO tipo_vehiculo (codigo, nombre) VALUES ('CAMIONETA', 'Camioneta');
INSERT INTO tarifa (tipo_vehiculo_id, valor_hora, vigente_desde)
VALUES ((SELECT id FROM tipo_vehiculo WHERE codigo = 'CAMIONETA'), 2500.00, NOW());
COMMIT;

-- Cambio anual de tarifa
BEGIN;
UPDATE tarifa
   SET vigente_hasta = '2027-01-01 00:00:00'
 WHERE tipo_vehiculo_id = (SELECT id FROM tipo_vehiculo WHERE codigo = 'CARRO')
   AND vigente_hasta IS NULL;
INSERT INTO tarifa (tipo_vehiculo_id, valor_hora, vigente_desde)
VALUES ((SELECT id FROM tipo_vehiculo WHERE codigo = 'CARRO'), 2000.00, '2027-01-01 00:00:00');
COMMIT;
*/

