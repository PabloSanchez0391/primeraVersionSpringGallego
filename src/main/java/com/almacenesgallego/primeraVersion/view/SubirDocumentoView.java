package com.almacenesgallego.primeraVersion.view;

import com.almacenesgallego.primeraVersion.model.Producto;
import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;
import com.almacenesgallego.primeraVersion.model.Proveedor;
import com.almacenesgallego.primeraVersion.model.ReferenciaProveedor;
import com.almacenesgallego.primeraVersion.repository.ProductoRepository;
import com.almacenesgallego.primeraVersion.repository.ProveedorRepository;
import com.almacenesgallego.primeraVersion.repository.ReferenciaProveedorRepository;
import com.almacenesgallego.primeraVersion.wrapper.ProductoAlbaranEditable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.dialog.Dialog;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "subir-documento", layout = MainView.class)
@RequiredArgsConstructor
public class SubirDocumentoView extends VerticalLayout {

    private final Grid<ProductoAlbaranEditable> grid = new Grid<>(ProductoAlbaranEditable.class, false);
//    private final AlbaranService albaranService;
//    private final ProveedorRepository proveedorRepository;
//    private final ProductoRepository productoRepository;
    private final ReferenciaProveedorRepository referenciaProveedorRepository;
    private Proveedor proveedorSeleccionado;
    private final H3 proveedorActualLabel = new H3("📦 Proveedor actual: (ninguno)");
    private Button addButton, guardarBtn;
    private HorizontalLayout accionesGridLayout;


    @PostConstruct
    private void init() {
        // Título
        add(new H3("Subir documento PDF para análisis"));

        configurarBotonesYDialog();

        // Configurar grid
        configurarGrid();

        // Agregar componentes a la UI
        add(grid);
    }

    private void configurarBotonesYDialog() {
        proveedorActualLabel.getStyle().set("color", "#1e88e5");
        proveedorActualLabel.getStyle().set("font-weight", "bold");
        proveedorActualLabel.getStyle().set("margin-bottom", "0.5em");

        // === Diálogo de subida ===
        Dialog uploadDialog = new Dialog();
        uploadDialog.setHeaderTitle("Subir documento PDF para análisis");
        uploadDialog.setWidth("500px");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/pdf");
        upload.setWidthFull();

        ComboBox<Proveedor> proveedorCombo = new ComboBox<>("Seleccionar proveedor");
        proveedorCombo.setItemLabelGenerator(Proveedor::getNombre);
        proveedorCombo.setPlaceholder("Selecciona un proveedor...");
//        proveedorCombo.setItems(proveedorRepository.findAll());
        List<Proveedor> proveedores = obtenerProveedoresRemoto();
        proveedorCombo.setItems(proveedores);
        proveedorCombo.setWidthFull();
        proveedorCombo.addValueChangeListener(e -> proveedorSeleccionado = e.getValue());

        Button analizarBtn = new Button("📄 Analizar PDF", e -> {
            if (proveedorSeleccionado == null) {
                Notification.show("Selecciona un proveedor antes de analizar el documento");
                return;
            }
            proveedorActualLabel.setText("📦 Proveedor actual: " + proveedorSeleccionado.getNombre());
            uploadDialog.close();
            procesarPDF(buffer);
        });
        analizarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelarBtn = new Button("Cancelar", e -> uploadDialog.close());
        HorizontalLayout accionesDialogo = new HorizontalLayout(analizarBtn, cancelarBtn);
        accionesDialogo.setWidthFull();
        accionesDialogo.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout contenidoDialogo = new VerticalLayout(proveedorCombo, upload, accionesDialogo);
        contenidoDialogo.setSpacing(true);
        contenidoDialogo.setAlignItems(Alignment.STRETCH);
        uploadDialog.add(contenidoDialogo);

        // === Botón principal (abre el diálogo) ===
        Button subirAnalizarBtn = new Button("Subir y analizar", e -> uploadDialog.open());
        subirAnalizarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // === Crear botones de acción (inicialmente ocultos) ===
        addButton = new Button("➕ Añadir producto", e -> {
            List<ProductoAlbaranEditable> items = new ArrayList<>(grid.getListDataView().getItems().toList());
            ProductoAlbaranEditable nuevo = new ProductoAlbaranEditable();
            nuevo.setCodigo("Nuevo");
            nuevo.setDescripcion("");
            nuevo.setCantidad(BigDecimal.ZERO);
            nuevo.setFechaCaducidad(LocalDate.now());
            nuevo.setNumeroLote("");
            items.add(nuevo);
            grid.setItems(items);
        });

        guardarBtn = new Button("💾 Guardar en base de datos", e -> {
            if (proveedorSeleccionado == null) {
                Notification.show("Selecciona un proveedor antes de guardar");
                return;
            }

            List<ProductoAlbaranEditable> productos = new ArrayList<>(grid.getListDataView().getItems().toList());

            try {
                List<ProductoAlbaranEditable> noReconocidos = productos.stream()
                        .filter(pa -> referenciaProveedorRepository
                                .findByProveedorAndCodigoProveedor(proveedorSeleccionado, pa.getCodigo())
                                .isEmpty())
                        .toList();

                if (noReconocidos.isEmpty()) {
//                    albaranService.guardarAlbaran(productos, proveedorSeleccionado);
                    guardarAlbaranRemoto(productos, proveedorSeleccionado.getId());

                    Notification.show("Productos guardados correctamente en la base de datos");
                } else {
                    procesarNoReconocidos(noReconocidos, 0, productos);
                }

            } catch (Exception ex) {
                Notification.show("Error guardando en base de datos: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Inicialmente ocultos
        addButton.setVisible(false);
        guardarBtn.setVisible(false);

        // === Layout que contendrá los botones debajo del grid ===
        accionesGridLayout = new HorizontalLayout(addButton, guardarBtn);
        accionesGridLayout.setSpacing(true);
        accionesGridLayout.setVisible(false);

        // === Barra superior con el botón principal ===
        HorizontalLayout barraSuperior = new HorizontalLayout(subirAnalizarBtn);
        barraSuperior.setSpacing(true);
        barraSuperior.setAlignItems(Alignment.CENTER);
        barraSuperior.getStyle().set("margin-bottom", "1em");

        add(proveedorActualLabel, barraSuperior, accionesGridLayout);
    }

    /**
     * Procesa el PDF subido y muestra los botones cuando haya resultados.
     */
    private void procesarPDF(MemoryBuffer buffer) {
        try (InputStream inputStream = buffer.getInputStream()) {
            if (inputStream == null) {
                Notification.show("Selecciona un PDF primero");
                return;
            }

            Path tempFile = Files.createTempFile("upload-", ".pdf");
            Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/pdf/upload"))
                    .header("Content-Type", "multipart/form-data; boundary=boundary123")
                    .POST(ofFileMultipart(tempFile, "file", "application/pdf", String.valueOf(proveedorSeleccionado.getId())))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            List<ProductoAlbaran> productos = Arrays.asList(
                    mapper.readValue(response.body(), ProductoAlbaran[].class)
            );

            List<ProductoAlbaranEditable> productosEditables = productos.stream()
                    .map(ProductoAlbaranEditable::new)
                    .collect(Collectors.toList());

            grid.setItems(productosEditables);

            // === Mostrar botones debajo del grid ===
            addButton.setVisible(true);
            guardarBtn.setVisible(true);
            accionesGridLayout.setVisible(true);

            Notification.show("Documento analizado con éxito: " + productos.size() + " productos detectados");
        } catch (Exception ex) {
            Notification.show("Error procesando PDF: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void configurarGrid() {
        TextField descripcionField = new TextField();
        TextField cantidadField = new TextField();
        TextField numeroLoteField = new TextField();

        Binder<ProductoAlbaranEditable> binder = new Binder<>(ProductoAlbaranEditable.class);
        Editor<ProductoAlbaranEditable> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        // === Columnas editables ===
        grid.addColumn(ProductoAlbaranEditable::getCodigo)
                .setHeader("Código");

        Grid.Column<ProductoAlbaranEditable> descripcionCol =
                grid.addColumn(ProductoAlbaranEditable::getDescripcion)
                        .setHeader("Descripción");
        binder.bind(descripcionField,
                ProductoAlbaranEditable::getDescripcion,
                ProductoAlbaranEditable::setDescripcion);
        descripcionCol.setEditorComponent(descripcionField);

        Grid.Column<ProductoAlbaranEditable> cantidadCol =
                grid.addColumn(pa -> pa.getCantidad() != null ? pa.getCantidad().toPlainString() : "")
                        .setHeader("Cantidad");
        binder.forField(cantidadField)
                .withConverter(new com.vaadin.flow.data.converter.StringToBigDecimalConverter("Número inválido"))
                .bind(ProductoAlbaranEditable::getCantidad, ProductoAlbaranEditable::setCantidad);
        cantidadCol.setEditorComponent(cantidadField);

        grid.addColumn(pa -> pa.getFechaCaducidad() != null ? pa.getFechaCaducidad().toString() : "")
                .setHeader("Fecha Caducidad");

        Grid.Column<ProductoAlbaranEditable> loteCol =
                grid.addColumn(ProductoAlbaranEditable::getNumeroLote)
                        .setHeader("Número Lote");
        binder.bind(numeroLoteField,
                ProductoAlbaranEditable::getNumeroLote,
                ProductoAlbaranEditable::setNumeroLote);
        loteCol.setEditorComponent(numeroLoteField);

        // === Botones de acción en cada fila ===
        grid.addComponentColumn(item -> {
            Button editar = new Button("✏️", e -> editor.editItem(item));
            Button eliminar = new Button("🗑️", e -> eliminarProducto(item));
            editar.getStyle().set("margin-right", "5px");
            return new HorizontalLayout(editar, eliminar);
        }).setHeader("Acciones");

        // Doble clic para editar
        grid.addItemDoubleClickListener(event -> {
            editor.editItem(event.getItem());
            descripcionField.focus();
        });

        // Teclas Enter / Escape
        grid.getElement().addEventListener("keydown", e -> editor.save())
                .setFilter("event.key === 'Enter'");
        grid.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.key === 'Escape'");

        grid.setWidthFull();
        grid.setAllRowsVisible(true);
    }

    /** Elimina un producto del grid */
    private void eliminarProducto(ProductoAlbaranEditable producto) {
        List<ProductoAlbaranEditable> items = new ArrayList<>(grid.getListDataView().getItems().toList());
        items.remove(producto);
        grid.setItems(items);
    }

    private void mostrarDialogoNuevaRelacion(ProductoAlbaranEditable pa, Runnable onGuardado) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Vincular nuevo producto");
        dialog.setWidth("800px");

        // Campos de información
        TextField codigoProveedorField = new TextField("Código proveedor");
        codigoProveedorField.setValue(pa.getCodigo());
        codigoProveedorField.setReadOnly(true);

        TextField descripcionField = new TextField("Descripción");
        descripcionField.setValue(pa.getDescripcion());
        descripcionField.setReadOnly(true);

        // ComboBox para seleccionar producto
        ComboBox<Producto> productoCombo = new ComboBox<>("Selecciona producto de la empresa");
        productoCombo.setItemLabelGenerator(p -> p.getId() + " - " + p.getNombre());
        productoCombo.setWidthFull();
        productoCombo.setAllowCustomValue(false); // No permitir valores libres

//        List<Producto> todosProductos = productoRepository.findAll();
        List<Producto> todosProductos = obtenerProductosRemoto();
        productoCombo.setItems(todosProductos); // Va a filtrar automáticamente por el label mientras escribes

        // Botones
        Button guardarBtn = new Button("Guardar", e -> {
            Producto productoSeleccionado = productoCombo.getValue();
            if (productoSeleccionado == null) {
                Notification.show("Selecciona un producto válido");
                return;
            }

            if (proveedorSeleccionado == null) {
                Notification.show("Selecciona un proveedor antes de crear la relación");
                return;
            }

            // Crear la nueva relación
            ReferenciaProveedor ref = ReferenciaProveedor.builder()
                    .proveedor(proveedorSeleccionado)
                    .producto(productoSeleccionado)
                    .codigoProveedor(pa.getCodigo())
                    .build();

            guardarReferenciaProveedorRemoto(ref, () -> {
                dialog.close();
                if (onGuardado != null) {
                    onGuardado.run();
                }
            });
        });

        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout botones = new HorizontalLayout(guardarBtn, cancelarBtn);
        botones.setWidthFull();
        botones.setJustifyContentMode(JustifyContentMode.END);

        dialog.setWidth("90%");
        dialog.setMaxWidth("600px");
        dialog.setHeight("auto");
        dialog.setMaxHeight("80%");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout contenido = new VerticalLayout(
                codigoProveedorField,
                descripcionField,
                productoCombo,
                botones
        );
        contenido.setWidthFull();
        contenido.setPadding(true);
        contenido.setSpacing(true);
        contenido.setAlignItems(FlexComponent.Alignment.STRETCH);

        dialog.add(contenido);
        dialog.open();
    }

    private void procesarNoReconocidos(List<ProductoAlbaranEditable> lista, int index, List<ProductoAlbaranEditable> todos) {
        if (index >= lista.size()) {
            try {
                guardarAlbaranRemoto(todos, proveedorSeleccionado.getId());
                Notification.show("Productos guardados correctamente en la base de datos");
            } catch (Exception ex) {
                Notification.show("Error guardando en base de datos: " + ex.getMessage());
                ex.printStackTrace();
            }
            return;
        }


        ProductoAlbaranEditable pa = lista.get(index);
        mostrarDialogoNuevaRelacion(pa, () -> procesarNoReconocidos(lista, index + 1, todos));
    }

    private static HttpRequest.BodyPublisher ofFileMultipart(Path filePath, String paramName, String mimeType, String proveedorId) throws Exception {
        String boundary = "boundary123";
        String CRLF = "\r\n";

        StringBuilder sb = new StringBuilder();

        // Campo del proveedor
        sb.append("--").append(boundary).append(CRLF);
        sb.append("Content-Disposition: form-data; name=\"proveedorId\"").append(CRLF);
        sb.append(CRLF).append(proveedorId).append(CRLF);

        // Campo del archivo
        sb.append("--").append(boundary).append(CRLF);
        sb.append("Content-Disposition: form-data; name=\"").append(paramName)
                .append("\"; filename=\"").append(filePath.getFileName()).append("\"").append(CRLF);
        sb.append("Content-Type: ").append(mimeType).append(CRLF).append(CRLF);

        var fileBytes = Files.readAllBytes(filePath);
        var closing = CRLF + "--" + boundary + "--" + CRLF;

        return HttpRequest.BodyPublishers.ofByteArrays(List.of(
                sb.toString().getBytes(),
                fileBytes,
                closing.getBytes()
        ));
    }

    private void guardarAlbaranRemoto(List<ProductoAlbaranEditable> productos, Integer proveedorId) throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String json = mapper.writeValueAsString(productos);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/albaranes/" + proveedorId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error guardando albarán: " + response.body());
        }
    }

    private void guardarReferenciaProveedorRemoto(ReferenciaProveedor ref, Runnable onSuccess) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(ref);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/referencias-proveedor"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Notification.show("Relación creada correctamente");
                if (onSuccess != null) onSuccess.run();
            } else {
                Notification.show("Error creando relación: " + response.body());
            }

        } catch (Exception ex) {
            Notification.show("Error en la conexión con el servidor: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private List<Proveedor> obtenerProveedoresRemoto() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/proveedores"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return Arrays.asList(mapper.readValue(response.body(), Proveedor[].class));
            } else {
                Notification.show("Error al cargar proveedores: " + response.body());
            }
        } catch (Exception ex) {
            Notification.show("Error conectando con el servidor: " + ex.getMessage());
            ex.printStackTrace();
        }
        return List.of(); // Devuelve lista vacía si falla
    }

    private List<Producto> obtenerProductosRemoto() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/productos"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return Arrays.asList(mapper.readValue(response.body(), Producto[].class));
            } else {
                Notification.show("Error al cargar productos: " + response.body());
            }

        } catch (Exception ex) {
            Notification.show("Error conectando con el servidor: " + ex.getMessage());
            ex.printStackTrace();
        }

        return List.of(); // Devuelve lista vacía si falla
    }

}
