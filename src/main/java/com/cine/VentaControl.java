package com.cine;

import javaDB.ButacaBD;
import javaDB.ClienteBD;
import javaDB.EntradaBD;
import javaDB.FuncionBD;
import javaDB.HistorialPuntosBD;
import javaDB.MetodoPagoBD;
import javaDB.PeliculaBD;
import javaDB.SalaBD;
import javaDB.TipoEntradaBD;
import javaDB.VentaBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import logico.Butaca;
import logico.Cliente;
import logico.Entrada;
import logico.Funcion;
import logico.MetodoPago;
import logico.Pelicula;
import logico.Sala;
import logico.TipoEntrada;
import logico.VentaDetalle;
import sesion.SesionActual;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Pantalla de Ventas. El canal (TAQUILLA/EN_LINEA) y el empleado NUNCA se
// eligen a mano: los decide SesionActual segun quien inicio sesion. Un
// Cliente compra para si mismo (no elige cliente); Administrador/Cajero
// eligen a que cliente le venden. Cada rol solo ve sus propias ventas,
// excepto Administrador que las ve todas (ver VentaBD.listarVentas).
public class VentaControl {

    private ClienteBD clienteBD = new ClienteBD();
    private MetodoPagoBD metodoPagoBD = new MetodoPagoBD();
    private TipoEntradaBD tipoEntradaBD = new TipoEntradaBD();
    private FuncionBD funcionBD = new FuncionBD();
    private PeliculaBD peliculaBD = new PeliculaBD();
    private SalaBD salaBD = new SalaBD();
    private ButacaBD butacaBD = new ButacaBD();
    private EntradaBD entradaBD = new EntradaBD();
    private VentaBD ventaBD = new VentaBD();

    @FXML private VBox filaClienteVenta;
    @FXML private ComboBox<Cliente> cmbVentaCliente;
    @FXML private ComboBox<MetodoPago> cmbVentaMetodoPago;
    @FXML private TextArea txtVentaObservacion;
    @FXML private ComboBox<Funcion> cmbVentaFuncion;
    @FXML private VBox ventaSeatGridContainer;
    @FXML private ComboBox<TipoEntrada> cmbVentaTipoEntrada;
    @FXML private Button btnRegistrarVenta;

    @FXML private Label lblUltimaVentaResumen;
    @FXML private ComboBox<String> cmbVentaFiltroEstado;
    @FXML private VBox ventasListaContainer;

    // Butacas marcadas en la cuadricula, cada una con el tipo de entrada que
    // estaba elegido en cmbVentaTipoEntrada en el momento de marcarla. Asi
    // una familia puede comprar, en una sola venta, butacas de tipos
    // distintos (BR-19): se elige el tipo, se toca la butaca, se cambia el
    // tipo, se toca la siguiente butaca, etc.
    private Map<Integer, TipoEntrada> butacasSeleccionadas = new LinkedHashMap<>();

    // Para no repetir la consulta a la BD cada vez que se cambia el filtro
    // de estado: se guarda la ultima lista traida y se repinta desde ahi.
    private List<VentaDetalle> ventasCargadas = new ArrayList<>();

    // Peliculas/salas en memoria, solo para mostrar el nombre en el combo de
    // funciones (mismo motivo que FuncionControl.buscarPeliculaPorId).
    private List<Pelicula> peliculas = new ArrayList<>();
    private List<Sala> salas = new ArrayList<>();

    @FXML
    private void initialize() {

        cmbVentaFiltroEstado.setValue("TODOS");
        configurarConversorDeFuncion();
        aplicarPermisosSegunRol();

        cmbVentaFuncion.valueProperty().addListener((obs, anterior, nuevaFuncion) -> cargarButacas(nuevaFuncion));

        cargarVentas();
    }

    // Si es Cliente, oculta la fila de "Cliente" (el comprador es el mismo
    // que inicio sesion). Si no, carga la lista de clientes para elegir.
    private void aplicarPermisosSegunRol() {

        if (SesionActual.esCliente()) {
            filaClienteVenta.setVisible(false);
            filaClienteVenta.setManaged(false);
        }
    }

    // Publico para que VentanaPrincipalControl lo llame cada vez que se
    // entra a "Ventas" (mismo patron que FuncionControl.cargarFunciones).
    public void cargarVentas() {

        if (!SesionActual.esCliente()) {
            cmbVentaCliente.getItems().setAll(clienteBD.listarClientes());
        }
        cmbVentaMetodoPago.getItems().setAll(metodoPagoBD.listarActivos());
        cmbVentaTipoEntrada.getItems().setAll(tipoEntradaBD.listarActivos());
        cmbVentaFuncion.getItems().setAll(funcionBD.listarProximasFunciones(50));
        peliculas = peliculaBD.listarPeliculas();
        salas = salaBD.listarSalas();

        Integer idCliente = SesionActual.esCliente() ? SesionActual.getClienteActivo().getIdCliente() : null;
        Integer idEmpleado = SesionActual.esCajero() ? SesionActual.getEmpleadoActivo().getIdEmpleado() : null;

        ventasCargadas = ventaBD.listarVentas(idCliente, idEmpleado);
        pintarListaVentas();
    }

    @FXML
    private void aplicarFiltroEstado() {
        pintarListaVentas();
    }

    // BR-19: una venta puede tener varias entradas (varias butacas). Como
    // VentaBD.listarVentas ya ordena por id_venta, las entradas de una
    // misma venta llegan siempre juntas: aqui se agrupan para pintar una
    // sola tarjeta por venta, con una subfila por butaca.
    private void pintarListaVentas() {

        ventasListaContainer.getChildren().clear();

        String filtro = cmbVentaFiltroEstado.getValue();
        List<VentaDetalle> entradasDeLaVentaActual = new ArrayList<>();

        for (VentaDetalle entrada : ventasCargadas) {

            if (filtro != null && !"TODOS".equals(filtro) && !filtro.equals(entrada.getEstadoVenta())) {
                continue;
            }

            if (!entradasDeLaVentaActual.isEmpty() && entradasDeLaVentaActual.get(0).getIdVenta() != entrada.getIdVenta()) {
                ventasListaContainer.getChildren().add(crearTarjetaVenta(entradasDeLaVentaActual));
                entradasDeLaVentaActual = new ArrayList<>();
            }

            entradasDeLaVentaActual.add(entrada);
        }

        if (!entradasDeLaVentaActual.isEmpty()) {
            ventasListaContainer.getChildren().add(crearTarjetaVenta(entradasDeLaVentaActual));
        }
    }

    // Para que el combo de funciones muestre "Pelicula - Sala - fecha hora"
    // en vez de solo la fecha (Funcion no conoce el titulo de su pelicula
    // ni el nombre de su sala, solo sus id).
    private void configurarConversorDeFuncion() {

        cmbVentaFuncion.setConverter(new StringConverter<>() {

            @Override
            public String toString(Funcion funcion) {
                if (funcion == null) {
                    return "";
                }
                return buscarTituloPelicula(funcion.getIdPelicula()) + " - " + buscarNombreSala(funcion.getIdSala())
                        + " - " + funcion.getFechaFuncion() + " " + funcion.getHoraInicio();
            }

            @Override
            public Funcion fromString(String texto) {
                return null; // el combo no se escribe a mano, solo se elige de la lista
            }
        });
    }

    private String buscarTituloPelicula(int idPelicula) {
        for (Pelicula pelicula : peliculas) {
            if (pelicula.getIdPelicula() == idPelicula) {
                return pelicula.getTitulo();
            }
        }
        return "Pelicula #" + idPelicula;
    }

    private String buscarNombreSala(int idSala) {
        for (Sala sala : salas) {
            if (sala.getIdSala() == idSala) {
                return sala.getNombreSala();
            }
        }
        return "Sala #" + idSala;
    }

    // Pinta un ToggleButton por butaca ACTIVA de la sala de la funcion
    // elegida, agrupadas por fila; deshabilita las que ya estan ocupadas.
    // No usan ToggleGroup a proposito: se puede marcar mas de una butaca
    // (BR-19), cada boton se prende/apaga independiente de los demas.
    private void cargarButacas(Funcion funcion) {

        ventaSeatGridContainer.getChildren().clear();
        butacasSeleccionadas.clear();

        if (funcion == null) {
            return;
        }

        List<Butaca> butacas = butacaBD.listarActivasDeSala(funcion.getIdSala());

        if (butacas.isEmpty()) {
            ventaSeatGridContainer.getChildren().add(new Label("Esta sala todavia no tiene butacas configuradas."));
            return;
        }

        List<Integer> ocupadas = entradaBD.listarIdsButacasOcupadas(funcion.getIdFuncion());

        String filaActual = null;
        HBox butacasDeLaFila = null;

        for (Butaca butaca : butacas) {

            if (!butaca.getFila().equals(filaActual)) {
                filaActual = butaca.getFila();

                Label etiquetaFila = new Label(filaActual);
                etiquetaFila.getStyleClass().add("seat-row-label");

                butacasDeLaFila = new HBox(6);

                HBox filaCompleta = new HBox(8, etiquetaFila, butacasDeLaFila);
                filaCompleta.setAlignment(Pos.CENTER);
                ventaSeatGridContainer.getChildren().add(filaCompleta);
            }

            ToggleButton botonButaca = new ToggleButton(String.valueOf(butaca.getNumero()));
            botonButaca.getStyleClass().add("seat-btn");

            boolean ocupada = ocupadas.contains(butaca.getIdButaca());
            botonButaca.setDisable(ocupada);

            if (!ocupada) {
                int idButaca = butaca.getIdButaca();
                botonButaca.selectedProperty().addListener((obs, antes, marcado) -> {
                    if (marcado) {
                        TipoEntrada tipoElegido = cmbVentaTipoEntrada.getValue();
                        if (tipoElegido == null) {
                            Alertas.mostrarAviso("Elige el tipo de entrada antes de marcar la butaca.");
                            botonButaca.setSelected(false);
                            return;
                        }
                        butacasSeleccionadas.put(idButaca, tipoElegido);
                    } else {
                        butacasSeleccionadas.remove(idButaca);
                    }
                });
            }

            butacasDeLaFila.getChildren().add(botonButaca);
        }
    }

    @FXML
    private void registrarVenta() {

        try {
            Funcion funcion = cmbVentaFuncion.getValue();
            MetodoPago metodoPago = cmbVentaMetodoPago.getValue();
            String observacion = txtVentaObservacion.getText().trim();

            if (funcion == null) {
                Alertas.mostrarAviso("Debes elegir una funcion.");
                return;
            }

            if (butacasSeleccionadas.isEmpty()) {
                Alertas.mostrarAviso("Debes elegir al menos una butaca.");
                return;
            }

            if (metodoPago == null) {
                Alertas.mostrarAviso("Debes elegir un metodo de pago.");
                return;
            }

            int idCliente;
            Integer idEmpleado;
            String canal;

            if (SesionActual.esCliente()) {
                idCliente = SesionActual.getClienteActivo().getIdCliente();
                idEmpleado = null;
                canal = "EN_LINEA";
            } else {
                Cliente cliente = cmbVentaCliente.getValue();
                if (cliente == null) {
                    Alertas.mostrarAviso("Debes elegir un cliente.");
                    return;
                }
                idCliente = cliente.getIdCliente();
                idEmpleado = SesionActual.getEmpleadoActivo().getIdEmpleado();
                canal = "TAQUILLA";
            }

            // BR-28/BR-30: una entrada de "Canje de puntos" (descuento del
            // 100%) solo debe poder venderse si el cliente ya tiene los 9
            // puntos que esa entrada le va a costar. Sin este chequeo se
            // podria generar una entrada gratuita que Fidelidad nunca
            // podria descontar (HistorialPuntosBD.registrarCanje exige el
            // mismo saldo minimo antes de dejar canjearla).
            boolean incluyeCanjeDePuntos = butacasSeleccionadas.values().stream()
                    .anyMatch(tipo -> tipo.getDescuentoPorcentaje().compareTo(BigDecimal.valueOf(100)) == 0);

            if (incluyeCanjeDePuntos && clienteBD.calcularPuntos(idCliente) < HistorialPuntosBD.PUNTOS_REQUERIDOS_CANJE) {
                Alertas.mostrarAviso("Este cliente no tiene los " + HistorialPuntosBD.PUNTOS_REQUERIDOS_CANJE
                        + " puntos de fidelidad necesarios para una entrada de canje de puntos.");
                return;
            }

            // BR-19: una llamada al procedimiento por butaca, cada una con su
            // propio tipo de entrada. La primera crea la venta (idVenta queda
            // en null); de ahi en adelante, todas se agregan a esa misma
            // venta en vez de crear una por butaca.
            Integer idVenta = null;
            List<Entrada> entradasRegistradas = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            for (Map.Entry<Integer, TipoEntrada> seleccion : butacasSeleccionadas.entrySet()) {
                Entrada entrada = ventaBD.registrarVenta(idCliente, idEmpleado, canal, metodoPago.getIdMetodoPago(),
                        observacion, funcion.getIdFuncion(), seleccion.getKey(), seleccion.getValue().getIdTipoEntrada(), idVenta);
                idVenta = entrada.getIdVenta();
                entradasRegistradas.add(entrada);
                total = total.add(entrada.getPrecioFinal());
            }

            lblUltimaVentaResumen.setText("Venta #" + idVenta + " registrada: " + entradasRegistradas.size()
                    + " butaca(s). Total: RD$" + total);

            if (Alertas.confirmar("¿Confirmar el pago de RD$" + total + " (" + entradasRegistradas.size() + " butaca(s)) ahora?")) {
                for (Entrada entrada : entradasRegistradas) {
                    ventaBD.confirmarPago(entrada.getIdEntrada());
                }
            }

            limpiarFormulario();
            cargarVentas();

        } catch (RuntimeException e) {
            // Cada butaca es su propia llamada al procedimiento (su propia
            // transaccion): si una falla a mitad del loop (ej. otra persona
            // tomo esa butaca primero), las anteriores ya quedaron
            // registradas. Se refresca la lista para que se vean.
            Alertas.mostrarAviso(e.getMessage());
            limpiarFormulario();
            cargarVentas();
        }
    }

    private void limpiarFormulario() {

        if (!SesionActual.esCliente()) {
            cmbVentaCliente.setValue(null);
        }
        cmbVentaMetodoPago.setValue(null);
        txtVentaObservacion.clear();
        cmbVentaFuncion.setValue(null);
        cmbVentaTipoEntrada.setValue(null);
        ventaSeatGridContainer.getChildren().clear();
        butacasSeleccionadas.clear();
    }

    // Una tarjeta = una venta, con una subfila por butaca/entrada (BR-19:
    // una venta puede tener varias). "Confirmar pago" y "Cancelar" son por
    // butaca, no por venta completa: se puede cancelar una sola butaca de
    // una compra de varias sin afectar las demas (BR-24). Como
    // VentaBD.listarVentas ya filtra por rol, todo lo que aparece aqui es
    // algo que el usuario con sesion iniciada puede tocar.
    private VBox crearTarjetaVenta(List<VentaDetalle> entradasDeLaVenta) {

        VentaDetalle primera = entradasDeLaVenta.get(0);

        Label titulo = new Label("Venta #" + primera.getIdVenta());
        titulo.getStyleClass().add("row-title");

        String origen = primera.getNombreEmpleado() != null
                ? primera.getNombreCliente() + " - TAQUILLA - " + primera.getNombreEmpleado()
                : primera.getNombreCliente() + " - EN_LINEA";
        Label subtitulo = new Label(origen + " - " + entradasDeLaVenta.size() + " butaca(s)");
        subtitulo.getStyleClass().add("row-subtitle");

        VBox textos = new VBox(2, titulo, subtitulo);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label monto = new Label("RD$" + primera.getTotalPagado());
        monto.getStyleClass().add("row-amount");

        Label estadoVenta = new Label(primera.getEstadoVenta());
        estadoVenta.getStyleClass().addAll("status-pill", "status-" + primera.getEstadoVenta().toLowerCase());

        HBox filaSuperior = new HBox(14, textos, espacio, monto, estadoVenta);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(10, filaSuperior);
        tarjeta.getStyleClass().add("list-row");

        for (VentaDetalle entrada : entradasDeLaVenta) {
            tarjeta.getChildren().add(crearSubfilaEntrada(entrada));
        }

        return tarjeta;
    }

    private HBox crearSubfilaEntrada(VentaDetalle entrada) {

        Label detalleEntrada = new Label("Butaca " + entrada.getFila() + entrada.getNumero() + " - "
                + entrada.getNombreTipoEntrada() + " - RD$" + entrada.getPrecioFinal());
        detalleEntrada.getStyleClass().add("funcion-subtitle");

        Label estadoEntrada = new Label(entrada.getEstadoEntrada());
        estadoEntrada.getStyleClass().addAll("status-pill", "status-" + entrada.getEstadoEntrada().toLowerCase());

        Region espacioSub = new Region();
        HBox.setHgrow(espacioSub, Priority.ALWAYS);

        HBox filaInferior = new HBox(10, detalleEntrada, estadoEntrada, espacioSub);
        filaInferior.setAlignment(Pos.CENTER_LEFT);
        filaInferior.getStyleClass().add("entrada-subrow");

        if ("RESERVADA".equals(entrada.getEstadoEntrada())) {
            Button btnConfirmarPago = new Button("Confirmar pago");
            btnConfirmarPago.getStyleClass().add("secondary-button");
            btnConfirmarPago.setOnAction(event -> confirmarPago(entrada));
            filaInferior.getChildren().add(btnConfirmarPago);
        }

        if (!"CANCELADA".equals(entrada.getEstadoEntrada())) {
            Button btnCancelar = new Button("Cancelar");
            btnCancelar.getStyleClass().add("icon-button");
            btnCancelar.setOnAction(event -> cancelarEntrada(entrada));
            filaInferior.getChildren().add(btnCancelar);
        }

        return filaInferior;
    }

    private void confirmarPago(VentaDetalle venta) {

        if (!Alertas.confirmar("¿Confirmar el pago de RD$" + venta.getPrecioFinal() + "?")) {
            return;
        }

        try {
            ventaBD.confirmarPago(venta.getIdEntrada());
            cargarVentas();
        } catch (RuntimeException e) {
            Alertas.mostrarAviso(e.getMessage());
        }
    }

    private void cancelarEntrada(VentaDetalle venta) {

        if (!Alertas.confirmar("¿Cancelar la entrada de la butaca " + venta.getFila() + venta.getNumero() + "?")) {
            return;
        }

        try {
            // EntradaBD.cancelarEntrada solo cancela si la funcion todavia
            // no ha terminado; si devuelve false, no hizo nada.
            boolean cancelada = entradaBD.cancelarEntrada(venta.getIdEntrada());
            if (!cancelada) {
                Alertas.mostrarAviso("No se pudo cancelar: la funcion ya termino.");
                return;
            }
            cargarVentas();
        } catch (RuntimeException e) {
            Alertas.mostrarAviso(e.getMessage());
        }
    }
}
