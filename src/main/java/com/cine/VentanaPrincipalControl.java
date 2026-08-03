package com.cine;

import javaDB.ConexionBD;
import javaDB.FuncionBD;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import logico.Usuario;
import sesion.SesionActual;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Controlador de ventana-principal.fxml: la barra lateral y el contenedor
// donde se muestra una pantalla a la vez. Tambien oculta del menu las
// pantallas que el rol con sesion iniciada no deberia ver.
public class VentanaPrincipalControl {

    @FXML private Button navPanel;
    @FXML private Button navPeliculas;
    @FXML private Button navSalas;
    @FXML private Button navFunciones;
    @FXML private Button navVentas;
    @FXML private Button navClientes;
    @FXML private Button navEmpleados;
    @FXML private Button navUsuarios;
    @FXML private Button navFidelidad;
    @FXML private Button navGeneros;

    @FXML private Label brandBadge;
    @FXML private Label lblUsuarioActivo;
    @FXML private Label lblRolActivo;
    @FXML private Button btnCerrarSesion;

    @FXML private VBox panelView;
    // Convencion de fx:include: fx:id="panelView" tambien inyecta el
    // controlador de esa vista en un campo llamado "panelViewController".
    // Se usa para refrescar las estadisticas cada vez que se abre el Panel.
    @FXML private PanelControl panelViewController;
    @FXML private VBox peliculasView;
    @FXML private PeliculaControl peliculasViewController;
    @FXML private VBox salasView;
    @FXML private SalaControl salasViewController;
    @FXML private VBox funcionesView;
    // Misma convencion que panelViewController: refresca los estados
    // (PROGRAMADA/EN_CURSO/FINALIZADA) cada vez que se abre esta pantalla,
    // no solo la primera vez que carga la app.
    @FXML private FuncionControl funcionesViewController;
    @FXML private VBox ventasView;
    @FXML private VentaControl ventasViewController;
    @FXML private VBox clientesView;
    @FXML private ClienteControl clientesViewController;
    @FXML private VBox empleadosView;
    @FXML private EmpleadoControl empleadosViewController;
    @FXML private VBox usuariosView;
    @FXML private UsuarioControl usuariosViewController;
    @FXML private VBox fidelidadView;
    // Misma convencion que ventasViewController: refresca el saldo/historial
    // del cliente consultado cada vez que se abre esta pantalla.
    @FXML private FidelidadControl fidelidadViewController;
    @FXML private VBox generosView;
    @FXML private GeneroControl generosViewController;

    // Para "despertar" el estado de las funciones antes de calcular el
    // checksum (ver revisarCambiosEnBD): si no, el estado nunca cambia solo
    // con el paso del tiempo y el checksum de "funcion" queda siempre igual.
    private final FuncionBD funcionBD = new FuncionBD();

    // Auto-refresco: cada 20s revisa en un hilo aparte (no en el de JavaFX,
    // para no congelar la interfaz) si cambio algo en la BD de la pantalla
    // que esta visible en ese momento, y si cambio la recarga.
    private final ScheduledExecutorService monitorBD = Executors.newSingleThreadScheduledExecutor(tarea -> {
        Thread hilo = new Thread(tarea, "monitor-cambios-bd");
        hilo.setDaemon(true);
        return hilo;
    });

    // Guardado por showView() (hilo de JavaFX) para que el monitor sepa que
    // pantalla esta visible sin leer el scene graph desde otro hilo.
    private volatile VBox vistaVisibleActual;
    // Vista y checksum de la ultima revision: si la pantalla visible cambio
    // desde entonces, el primer chequeo solo guarda la base nueva, sin recargar.
    private volatile VBox vistaDelUltimoChecksum;
    private volatile String ultimoChecksumRevisado;

    // Cuenta fallos seguidos del chequeo; si llega al limite, se avisa una
    // vez para que no quede fallando en silencio.
    private int fallosSeguidosDelMonitor;
    private static final int FALLOS_SEGUIDOS_PARA_AVISAR = 3;

    @FXML
    private void initialize() {

        mostrarUsuarioActivo();
        aplicarPermisosPorRol();

        navPanel.setOnAction(event -> {
            showView(panelView, navPanel);
            panelViewController.cargarEstadisticas();
        });
        navPeliculas.setOnAction(event -> {
            showView(peliculasView, navPeliculas);
            peliculasViewController.recargarGeneros();
            peliculasViewController.cargarPeliculas();
        });
        navSalas.setOnAction(event -> {
            showView(salasView, navSalas);
            salasViewController.cargarSalas();
        });
        navFunciones.setOnAction(event -> {
            showView(funcionesView, navFunciones);
            funcionesViewController.cargarFunciones();
        });
        navVentas.setOnAction(event -> {
            showView(ventasView, navVentas);
            ventasViewController.cargarVentas();
        });
        navClientes.setOnAction(event -> {
            showView(clientesView, navClientes);
            clientesViewController.cargarClientes();
        });
        navEmpleados.setOnAction(event -> {
            showView(empleadosView, navEmpleados);
            empleadosViewController.cargarEmpleados();
        });
        navUsuarios.setOnAction(event -> {
            showView(usuariosView, navUsuarios);
            usuariosViewController.cargarUsuarios();
        });
        navFidelidad.setOnAction(event -> {
            showView(fidelidadView, navFidelidad);
            fidelidadViewController.cargarFidelidad();
        });
        navGeneros.setOnAction(event -> {
            showView(generosView, navGeneros);
            generosViewController.cargarGeneros();
            // Si desde aca se creo/edito un genero, el checkbox de generos
            // del formulario de Peliculas tambien debe quedar al dia.
            peliculasViewController.recargarGeneros();
        });
        btnCerrarSesion.setOnAction(event -> cerrarSesion());

        mostrarPantallaInicial();
        iniciarAutoRefresco();
    }

    private void iniciarAutoRefresco() {
        monitorBD.scheduleWithFixedDelay(this::revisarCambiosEnBD, 20, 20, TimeUnit.SECONDS);
    }

    // Corre en el hilo del monitor, no en el de JavaFX. Pide el checksum de
    // la pantalla visible y si cambio, manda la recarga de vuelta al hilo
    // de JavaFX con Platform.runLater.
    private void revisarCambiosEnBD() {

        VBox vista = vistaVisibleActual;
        String[] tablas = tablasRelevantesDe(vista);

        if (vista == null || tablas == null) {
            return;
        }

        try {
            // El estado de una funcion (PROGRAMADA/EN_CURSO/FINALIZADA) no
            // cambia solo con el paso del tiempo: solo se recalcula cuando
            // se hace un UPDATE sobre "funcion" (dispara el trigger). Sin
            // este empujon, el checksum de esa tabla quedaria siempre igual
            // y el monitor nunca notaria que ya toca pasar a EN_CURSO/FINALIZADA.
            if (dependeDeFuncion(tablas)) {
                funcionBD.actualizarEstadosAutomaticos();
            }

            String checksum = ConexionBD.calcularChecksum(tablas);

            boolean mismaVistaQueLaVezPasada = vista == vistaDelUltimoChecksum;
            boolean cambio = mismaVistaQueLaVezPasada && !checksum.equals(ultimoChecksumRevisado);

            vistaDelUltimoChecksum = vista;
            ultimoChecksumRevisado = checksum;
            fallosSeguidosDelMonitor = 0;

            if (cambio) {
                Platform.runLater(() -> recargarVista(vista));
            }

        } catch (Exception e) {
            // Se loguea la excepcion completa por si getMessage() viene null.
            System.out.println("No se pudo revisar cambios en la base de datos ("
                    + e.getClass().getSimpleName() + "): " + e.getMessage());

            fallosSeguidosDelMonitor++;

            // Avisa una sola vez (no en cada intento) que el auto-refresco
            // dejo de funcionar, para que se sepa refrescar a mano mientras tanto.
            if (fallosSeguidosDelMonitor == FALLOS_SEGUIDOS_PARA_AVISAR) {
                Platform.runLater(() -> Alertas.mostrarAviso(
                        "La actualizacion automatica cada 20 segundos no esta pudiendo conectarse a la base de datos. "
                                + "Los datos de esta pantalla pueden estar desactualizados; navega a otra pantalla y vuelve para refrescarlos a mano mientras tanto."));
            }
        }
    }

    private boolean dependeDeFuncion(String[] tablas) {
        for (String tabla : tablas) {
            if ("funcion".equals(tabla)) {
                return true;
            }
        }
        return false;
    }

    // Tablas que le conciernen a cada pantalla, para pedir el checksum solo
    // de lo que esa pantalla muestra. null = pantalla sin auto-refresco.
    private String[] tablasRelevantesDe(VBox vista) {

        if (vista == panelView) return new String[]{"venta", "entrada", "funcion", "pelicula", "cliente"};
        if (vista == peliculasView) return new String[]{"pelicula", "pelicula_genero", "genero"};
        if (vista == salasView) return new String[]{"sala", "butaca"};
        if (vista == funcionesView) return new String[]{"funcion", "pelicula", "sala"};
        if (vista == ventasView) return new String[]{"venta", "entrada", "butaca", "funcion"};
        if (vista == clientesView) return new String[]{"cliente", "persona", "historial_puntos"};
        if (vista == empleadosView) return new String[]{"empleado", "persona"};
        if (vista == usuariosView) return new String[]{"usuario", "persona"};
        if (vista == fidelidadView) return new String[]{"historial_puntos", "cliente"};
        if (vista == generosView) return new String[]{"genero"};
        return null;
    }

    // Mismo metodo que ya llama cada boton de navegacion al hacer clic,
    // reutilizado aca para cuando el cambio lo detecta el monitor en vez de
    // un clic del usuario.
    private void recargarVista(VBox vista) {

        if (vista == panelView) {
            panelViewController.cargarEstadisticas();
        } else if (vista == peliculasView) {
            peliculasViewController.recargarGeneros();
            peliculasViewController.cargarPeliculas();
        } else if (vista == salasView) {
            salasViewController.cargarSalas();
        } else if (vista == funcionesView) {
            funcionesViewController.cargarFunciones();
        } else if (vista == ventasView) {
            ventasViewController.cargarVentas();
        } else if (vista == clientesView) {
            clientesViewController.cargarClientes();
        } else if (vista == empleadosView) {
            empleadosViewController.cargarEmpleados();
        } else if (vista == usuariosView) {
            usuariosViewController.cargarUsuarios();
        } else if (vista == fidelidadView) {
            fidelidadViewController.cargarFidelidad();
        } else if (vista == generosView) {
            generosViewController.cargarGeneros();
            peliculasViewController.recargarGeneros();
        }
    }

    private void mostrarUsuarioActivo() {

        Usuario usuario = SesionActual.getUsuarioActivo();

        lblUsuarioActivo.setText(usuario.getNombres() + " " + usuario.getApellidos());
        lblRolActivo.setText(usuario.getRol());
        brandBadge.setText(usuario.getRol());
    }

    // Que pantallas ve cada rol. Quien puede editar (no solo ver) se
    // controla aparte en el controlador de cada pantalla.
    private void aplicarPermisosPorRol() {

        boolean esAdmin = SesionActual.esAdministrador();
        boolean esCajero = SesionActual.esCajero();

        mostrarUOcultar(navPanel, esAdmin);
        mostrarUOcultar(navPeliculas, esAdmin || esCajero);
        mostrarUOcultar(navSalas, esAdmin || esCajero);
        mostrarUOcultar(navFunciones, esAdmin);
        mostrarUOcultar(navClientes, esAdmin || esCajero);
        mostrarUOcultar(navEmpleados, esAdmin);
        mostrarUOcultar(navUsuarios, esAdmin);
        mostrarUOcultar(navGeneros, esAdmin || esCajero);
        // navVentas y navFidelidad quedan visibles para los 3 roles.
    }

    private void mostrarUOcultar(Button boton, boolean visible) {
        boton.setVisible(visible);
        boton.setManaged(visible);
    }

    // El FXML deja "Panel" seleccionado por defecto, pero Panel es exclusivo
    // de ADMINISTRADOR (ver aplicarPermisosPorRol): CAJERO/CLIENTE deben
    // arrancar en una pantalla a la que sí tengan acceso.
    private void mostrarPantallaInicial() {

        if (SesionActual.esAdministrador()) {
            showView(panelView, navPanel);
            panelViewController.cargarEstadisticas();
        } else {
            showView(ventasView, navVentas);
            ventasViewController.cargarVentas();
        }
    }

    private void cerrarSesion() {

        try {
            // Si no se para, el monitor sigue corriendo en segundo plano
            // despues de volver al login, y cada sesion nueva levantaria otro mas.
            monitorBD.shutdownNow();

            SesionActual.cerrarSesion();
            Pantallas.cambiarA(btnCerrarSesion, "/com/cine/login.fxml", "Cinéma - Iniciar sesión", "/com/cine/styles.css", "/com/cine/login.css");

        } catch (IOException e) {
            Alertas.mostrarAviso("No se pudo cerrar la sesion: " + e.getMessage());
        }
    }

    private void showView(VBox selectedView, Button selectedNav) {

        vistaVisibleActual = selectedView;

        VBox[] views = {
                panelView,
                peliculasView,
                salasView,
                funcionesView,
                ventasView,
                clientesView,
                empleadosView,
                usuariosView,
                fidelidadView,
                generosView
        };
        for (VBox view : views) {
            boolean selected = view == selectedView;
            view.setVisible(selected);
            view.setManaged(selected);
        }

        Button[] buttons = {
                navPanel,
                navPeliculas,
                navSalas,
                navFunciones,
                navVentas,
                navClientes,
                navEmpleados,
                navUsuarios,
                navFidelidad,
                navGeneros
        };
        for (Button button : buttons) {
            button.getStyleClass().remove("nav-item-selected");
        }
        if (!selectedNav.getStyleClass().contains("nav-item-selected")) {
            selectedNav.getStyleClass().add("nav-item-selected");
        }
    }
}
