package com.parqueadero.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.parqueadero.datos.jdbc.ConexionBD;
import com.parqueadero.datos.jdbc.EstadiaRepositoryJdbc;
import com.parqueadero.datos.jdbc.GestorTransaccionesJdbc;
import com.parqueadero.datos.jdbc.TarifaRepositoryJdbc;
import com.parqueadero.datos.jdbc.TipoVehiculoRepositoryJdbc;
import com.parqueadero.datos.jdbc.UsuarioRepositoryJdbc;
import com.parqueadero.datos.jdbc.VehiculoRepositoryJdbc;
import com.parqueadero.dominio.cobro.CobroPorHoraIniciada;
import com.parqueadero.negocio.servicio.EstadiaService;
import com.parqueadero.negocio.servicio.TipoVehiculoService;
import com.parqueadero.negocio.servicio.VehiculoService;
import com.parqueadero.negocio.servicio.impl.EstadiaServiceImpl;
import com.parqueadero.negocio.servicio.impl.TipoVehiculoServiceImpl;
import com.parqueadero.negocio.servicio.impl.VehiculoServiceImpl;
import com.parqueadero.presentacion.controlador.EstadiaController;
import com.parqueadero.presentacion.controlador.VehiculoController;
import com.parqueadero.presentacion.vista.PanelIngreso;
import com.parqueadero.presentacion.vista.PanelPagoCaja;
import com.parqueadero.presentacion.vista.PanelSalida;
import com.parqueadero.presentacion.vista.PanelVehiculos;
import com.parqueadero.presentacion.vista.VentanaPrincipal;

public class Aplicacion {

    private static final Logger LOG = Logger.getLogger(Aplicacion.class.getName());

    // Sin inicio de sesión en el prototipo, el pago en caja se registra a nombre de
    // este usuario
    public static final String USUARIO_CAJA = "personal";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Aplicacion::iniciar);
    }

    private static void iniciar() {
        usarAparienciaDelSistema();
        try {
            GestorTransaccionesJdbc gestor = new GestorTransaccionesJdbc(new ConexionBD());
            verificarBaseDeDatos(gestor);

            ensamblar(gestor, Clock.systemDefaultZone()).setVisible(true);
        } catch (RuntimeException e) {

            LOG.log(Level.SEVERE, "No se pudo iniciar la aplicación", e);
            JOptionPane.showMessageDialog(null, mensajeDeArranque(e),
                    "No se pudo iniciar el sistema", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    static VentanaPrincipal ensamblar(GestorTransaccionesJdbc gestor, Clock reloj) {

        // Datos
        EstadiaRepositoryJdbc estadiaRepository = new EstadiaRepositoryJdbc(gestor);
        VehiculoRepositoryJdbc vehiculoRepository = new VehiculoRepositoryJdbc(gestor);
        TipoVehiculoRepositoryJdbc tipoVehiculoRepository = new TipoVehiculoRepositoryJdbc(gestor);
        TarifaRepositoryJdbc tarifaRepository = new TarifaRepositoryJdbc(gestor);
        UsuarioRepositoryJdbc usuarioRepository = new UsuarioRepositoryJdbc(gestor);

        // Negocio
        EstadiaService estadiaService = new EstadiaServiceImpl(gestor, estadiaRepository, vehiculoRepository,
                tipoVehiculoRepository, tarifaRepository, usuarioRepository,
                new CobroPorHoraIniciada(), USUARIO_CAJA, reloj);
        VehiculoService vehiculoService = new VehiculoServiceImpl(gestor, vehiculoRepository,
                tipoVehiculoRepository, estadiaRepository);
        TipoVehiculoService tipoVehiculoService = new TipoVehiculoServiceImpl(gestor, tipoVehiculoRepository);

        // Presentación
        PanelVehiculos panelVehiculos = new PanelVehiculos();
        PanelIngreso panelIngreso = new PanelIngreso();
        PanelSalida panelSalida = new PanelSalida();
        PanelPagoCaja panelPagoCaja = new PanelPagoCaja();

        VehiculoController vehiculoController = new VehiculoController(vehiculoService, tipoVehiculoService,
                panelVehiculos, panelIngreso);
        EstadiaController estadiaController = new EstadiaController(estadiaService,
                panelIngreso, panelSalida, panelPagoCaja, panelVehiculos);

        vehiculoController.iniciar();
        estadiaController.iniciar();

        return new VentanaPrincipal(panelVehiculos, panelIngreso, panelSalida, panelPagoCaja);
    }

    // Falla pronto y con un mensaje claro si no hay conexión o falta ejecutar el
    private static void verificarBaseDeDatos(GestorTransaccionesJdbc gestor) {
        gestor.ejecutar(() -> {
            try (Statement consulta = gestor.conexionActual().createStatement();
                    ResultSet resultado = consulta.executeQuery("SELECT COUNT(*) FROM tipo_vehiculo WHERE activo")) {
                resultado.next();
                LOG.info("Conexión a PostgreSQL correcta: " + resultado.getInt(1) + " tipos de vehículo activos.");
                return null;
            } catch (SQLException e) {
                throw new IllegalStateException("La base de datos no tiene las tablas del sistema: "
                        + "ejecute documentos/script/parqueadero.sql.", e);
            }
        });
    }

    private static String mensajeDeArranque(RuntimeException e) {
        String mensaje = e.getMessage();
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            mensaje += "\n\nDetalle: " + e.getCause().getMessage();
        }
        return mensaje + "\n\nRevise que PostgreSQL esté encendido y los datos de db.properties.";
    }

    private static void usarAparienciaDelSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
    }
}
