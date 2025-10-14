package com.almacenesgallego.primeraVersion.view;

import com.almacenesgallego.primeraVersion.model.Producto;
import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;
import com.almacenesgallego.primeraVersion.model.Proveedor;
import com.almacenesgallego.primeraVersion.model.ReferenciaProveedor;
import com.almacenesgallego.primeraVersion.repository.ProductoRepository;
import com.almacenesgallego.primeraVersion.repository.ProveedorRepository;
import com.almacenesgallego.primeraVersion.repository.ReferenciaProveedorRepository;
import com.almacenesgallego.primeraVersion.service.AlbaranService;
import com.almacenesgallego.primeraVersion.wrapper.ProductoAlbaranEditable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.formlayout.FormLayout;

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
import java.util.Optional;
import java.util.stream.Collectors;

@Route("subir-documento")
public class SubirDocumentoView extends VerticalLayout {

    private final Grid<ProductoAlbaranEditable> grid = new Grid<>(ProductoAlbaranEditable.class, false);
    private final AlbaranService albaranService;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final ReferenciaProveedorRepository referenciaProveedorRepository;


    public SubirDocumentoView(AlbaranService albaranService,
                              ProveedorRepository proveedorRepository,
                              ProductoRepository productoRepository,
                              ReferenciaProveedorRepository referenciaProveedorRepository) {
        this.albaranService = albaranService;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.referenciaProveedorRepository = referenciaProveedorRepository;

        add(new H3("Subir documento PDF para análisis"));

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/pdf");

        Button procesarBtn = new Button("Subir y analizar", e -> procesarPDF(buffer));

        configurarGrid(); // ✅ Limpieza visual

        Anchor volver = new Anchor("/", "Volver a la tabla");
        add(upload, procesarBtn, grid, volver);
    }

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
                    .POST(ofFileMultipart(tempFile, "file", "application/pdf"))
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

        // === Guardar o cancelar edición ===
        Button saveButton = new Button("Guardar", e -> editor.save());
        Button cancelButton = new Button("Cancelar", e -> editor.cancel());
        HorizontalLayout botonesEdicion = new HorizontalLayout(saveButton, cancelButton);
        add(botonesEdicion);

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

        // === Botón para añadir una nueva fila ===
        Button addButton = new Button("➕ Añadir producto", e -> {
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

        add(addButton);

        Button guardarBtn = new Button("💾 Guardar en base de datos", e -> {
            List<ProductoAlbaranEditable> productos = new ArrayList<>(grid.getListDataView().getItems().toList());

            try {
                Proveedor proveedor = proveedorRepository.findById(1)
                        .orElseThrow(() -> new RuntimeException("Proveedor con id=1 no encontrado"));

                List<ProductoAlbaranEditable> noReconocidos = productos.stream()
                        .filter(pa -> referenciaProveedorRepository.findByProveedorAndCodigoProveedor(proveedor, pa.getCodigo()).isEmpty())
                        .toList();

                if (noReconocidos.isEmpty()) {
                    albaranService.guardarAlbaran(productos);
                    Notification.show("Productos guardados correctamente en la base de datos");
                } else {
                    procesarNoReconocidos(noReconocidos, 0, productos, proveedor);
                }

            } catch (Exception ex) {
                Notification.show("Error guardando en base de datos: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        add(guardarBtn);
    }

    /** Elimina un producto del grid */
    private void eliminarProducto(ProductoAlbaranEditable producto) {
        List<ProductoAlbaranEditable> items = new ArrayList<>(grid.getListDataView().getItems().toList());
        items.remove(producto);
        grid.setItems(items);
    }

    private void mostrarDialogoNuevoProducto(ProductoAlbaranEditable pa, Runnable onGuardado) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Vincular nuevo producto");

        // Campos de información
        TextField codigoProveedorField = new TextField("Código proveedor");
        codigoProveedorField.setValue(pa.getCodigo());
        codigoProveedorField.setReadOnly(true);

        TextField descripcionField = new TextField("Descripción");
        descripcionField.setValue(pa.getDescripcion());
        descripcionField.setReadOnly(true);

        TextField codigoInternoField = new TextField("Código interno del producto");
        codigoInternoField.setPlaceholder("Ej: PRD001");

        // Botones
        Button guardarBtn = new Button("Guardar", e -> {
            String codigoInterno = codigoInternoField.getValue().trim();
            if (codigoInterno.isEmpty()) {
                Notification.show("Introduce un código interno válido");
                return;
            }

            Optional<Producto> productoOpt = productoRepository.findById(codigoInterno);
            if (productoOpt.isEmpty()) {
                Notification.show("No existe un producto con ese código interno");
                return;
            }

            Proveedor proveedor = proveedorRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Proveedor con id=1 no encontrado"));

            // Crear la nueva relación
            ReferenciaProveedor ref = ReferenciaProveedor.builder()
                    .proveedor(proveedor)
                    .producto(productoOpt.get())
                    .codigoProveedor(pa.getCodigo())
                    .build();

            referenciaProveedorRepository.save(ref);

            Notification.show("Relación creada correctamente");

            dialog.close();

            if (onGuardado != null) {
                onGuardado.run();
            }

        });

        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout botones = new HorizontalLayout(guardarBtn, cancelarBtn);
        VerticalLayout contenido = new VerticalLayout(
                codigoProveedorField,
                descripcionField,
                codigoInternoField,
                botones
        );
        contenido.setPadding(false);
        dialog.add(contenido);

        dialog.open();
    }

    private void procesarNoReconocidos(List<ProductoAlbaranEditable> lista, int index, List<ProductoAlbaranEditable> todos, Proveedor proveedor) {
        if (index >= lista.size()) {
            // Todos los no reconocidos procesados → guardar todo
            albaranService.guardarAlbaran(todos);
            Notification.show("Productos guardados correctamente en la base de datos");
            return;
        }

        ProductoAlbaranEditable pa = lista.get(index);
        mostrarDialogoNuevoProducto(pa, () -> procesarNoReconocidos(lista, index + 1, todos, proveedor));
    }


    private static HttpRequest.BodyPublisher ofFileMultipart(Path filePath, String paramName, String mimeType) throws Exception {
        String boundary = "boundary123";
        String CRLF = "\r\n";

        StringBuilder sb = new StringBuilder();
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
}
