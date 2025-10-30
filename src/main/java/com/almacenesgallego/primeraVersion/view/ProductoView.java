package com.almacenesgallego.primeraVersion.view;

import com.almacenesgallego.primeraVersion.view.components.BuscadorGenerico;
import com.almacenesgallego.primeraVersion.view.components.BuscadorProductos;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.almacenesgallego.primeraVersion.model.Producto;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import com.almacenesgallego.primeraVersion.view.components.EditableGrid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.component.textfield.NumberField;
import java.math.BigDecimal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;


@Route(value = "productos", layout = MainView.class)
@PageTitle("Listado de Productos")
public class ProductoView extends VerticalLayout {

    private final WebClient webClient;
    private final EditableGrid<Producto> grid = new EditableGrid<>(Producto.class) {

        @Override
        protected void buildEditForm(FormLayout form, Binder<Producto> binder, Producto item) {
            // === Campos del diálogo de edición ===
            TextField idField = new TextField("ID");
            idField.setReadOnly(true);

            TextField nombreField = new TextField("Nombre");
            nombreField.setWidthFull();

            NumberField precioField = new NumberField("Precio (€)");
            precioField.setWidthFull();

            // === Bindings ===
            binder.forField(idField)
                    .bind(Producto::getId, null); // 🔹 Solo lectura, no se asigna setter

            binder.forField(nombreField)
                    .asRequired("El nombre es obligatorio")
                    .bind(Producto::getNombre, Producto::setNombre);

            binder.forField(precioField)
                    .withConverter(
                            valueFromUI -> valueFromUI == null ? null : BigDecimal.valueOf(valueFromUI), // UI → BigDecimal
                            valueFromModel -> valueFromModel == null ? 0.0 : valueFromModel.doubleValue(), // BigDecimal → UI
                            "Debe ser un número válido"
                    )
                    .bind(Producto::getPrecio, Producto::setPrecio);

            form.add(idField, nombreField, precioField);

            binder.readBean(item);
        }

        @Override
        protected void onSave(Producto item) {
            try {
                webClient.put()
                        .uri("/{id}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(item)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();

                Notification.show("Producto actualizado: " + item.getNombre());
                cargarDatos();
            } catch (Exception e) {
                Notification.show("Error al actualizar: " + e.getMessage());
            }
        }
    };

    private final BuscadorGenerico<Producto> buscador;
//    private final TextField buscador = new TextField("Buscar producto");
//    private final BuscadorProductos buscador;
    private List<Producto> listaProductos;

    public ProductoView() {
        webClient = WebClient.create("http://localhost:8080/api/productos");


        buscador = new BuscadorGenerico<>(
                "Buscar producto",
                grid,
                listaProductos,
                (producto, texto) -> producto.getNombre().toLowerCase().contains(texto)
                        || producto.getId().toLowerCase().contains(texto)
        );

//        buscador = new BuscadorProductos(grid, listaProductos);
//        configurarBuscador();
        configurarGrid();
        cargarDatos();

        // 🔹 Botones debajo del grid
        HorizontalLayout botonesLayout = configurarBotones();

        // 🔹 Diseño general
        setSizeFull();
        setPadding(true);
        setSpacing(false);

        add(buscador, grid, botonesLayout);
        expand(grid);
    }

//    private void configurarBuscador() {
//        buscador.setPlaceholder("Escribe un nombre o ID...");
//        buscador.setClearButtonVisible(true);
//        buscador.setWidth("300px");
//        buscador.setValueChangeMode(ValueChangeMode.EAGER);
//        buscador.addValueChangeListener(event -> filtrar(event.getValue()));
//    }

    private void configurarGrid() {
        grid.removeAllColumns();

        grid.addTextColumn(Producto::getId, "ID");
        grid.addTextColumn(Producto::getNombre, "Nombre");
        grid.addTextColumn(p -> p.getPrecio() != null ? p.getPrecio().toString() : "", "Precio (€)");

        // Botones de acción (editar y eliminar)
        grid.addAccionesColumn(this::eliminarProducto);

        grid.setSizeFull();
    }

    private HorizontalLayout configurarBotones() {
        Button btnNuevo = new Button("Añadir");
        Button btnImportar = new Button("Importar CSV");
        Button btnEliminar = new Button("Eliminar");

        // 🔹 Acciones básicas (puedes ampliarlas después)
        btnNuevo.addClickListener(e -> Notification.show("Acción: Añadir producto"));

        // TODO implementar metodo el back
//        btnImportar.addClickListener(e -> abrirDialogImportarCSV());
        btnImportar.addClickListener(e -> Notification.show("Haz el metodo en el back melon"));


        btnEliminar.addClickListener(e -> {
            Producto seleccionado = grid.asSingleSelect().getValue();
            if (seleccionado != null)
                Notification.show("Eliminar: " + seleccionado.getNombre());
            else
                Notification.show("Selecciona un producto para eliminar.");
        });

        HorizontalLayout layout = new HorizontalLayout(btnNuevo, btnImportar, btnEliminar);
        layout.setWidthFull();
        layout.setJustifyContentMode(JustifyContentMode.END); // 🔹 alineados a la derecha
        layout.setPadding(true);

        return layout;
    }

    private void mostrarDialogoNuevoProducto() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Añadir nuevo producto");

        TextField idField = new TextField("ID del producto");
        TextField nombreField = new TextField("Nombre");
        NumberField precioField = new NumberField("Precio (€)");

        Button btnGuardar = new Button("Guardar", event -> {
            if (nombreField.isEmpty() || precioField.isEmpty()) {
                Notification.show("Por favor, completa todos los campos.");
                return;
            }

            try {
                Producto nuevo = new Producto();
                nuevo.setId(idField.getValue());
                nuevo.setNombre(nombreField.getValue());
                nuevo.setPrecio(BigDecimal.valueOf(precioField.getValue()));

                Producto guardado = webClient.post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(nuevo)
                        .retrieve()
                        .bodyToMono(Producto.class)
                        .block();

                if (guardado != null) {
                    Notification.show("Producto guardado: " + guardado.getNombre());
                    dialog.close();
                    cargarDatos();
                }
            } catch (Exception ex) {
                Notification.show("Error al guardar: " + ex.getMessage());
            }
        });

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout botones = new HorizontalLayout(btnGuardar, btnCancelar);
        botones.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout contenido = new VerticalLayout(idField, nombreField, precioField, botones);
        contenido.setPadding(false);
        contenido.setSpacing(true);

        dialog.add(contenido);
        dialog.open();
    }

    private void abrirDialogImportarCSV() {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".csv");
        upload.setMaxFiles(1);
        upload.setDropAllowed(true);

        upload.addSucceededListener(event -> {
            InputStream inputStream = buffer.getInputStream();
            // Llamada al backend enviando el CSV
            try {
                webClient.post()
                        .uri("http://localhost:8080/api/productos/importar") // tu endpoint de importación
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .bodyValue(inputStream.readAllBytes())
                        .retrieve()
                        .bodyToMono(Void.class)
                        .doOnError(err -> Notification.show("Error al importar CSV: " + err.getMessage()))
                        .doOnSuccess(res -> {
                            Notification.show("✅ CSV importado correctamente");
                            cargarDatos(); // recargar lista después de importar
                            dialog.close();
                        })
                        .subscribe();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        dialog.add(upload);
        dialog.open();
    }

    private void cargarDatos() {
        try {
            listaProductos = webClient.get()
                    .retrieve()
                    .bodyToFlux(Producto.class)
                    .collectList()
                    .block();

            grid.setItems(listaProductos);
            if (buscador != null) buscador.setListaOriginal(listaProductos);

//            if (buscador != null) buscador.setListaProductos(listaProductos); // 🔹 refresca el buscador

        } catch (Exception e) {
            Notification.show("Error cargando productos: " + e.getMessage());
        }
    }

    private void eliminarProducto(Producto producto) {
        try {
            webClient.delete()
                    .uri("/{id}", producto.getId())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            Notification.show("Producto eliminado: " + producto.getNombre());
            cargarDatos();
        } catch (Exception e) {
            Notification.show("Error al eliminar: " + e.getMessage());
        }
    }

//    private void filtrar(String filtro) {
//        if (listaProductos == null || listaProductos.isEmpty()) return;
//
//        if (filtro == null || filtro.trim().isEmpty()) {
//            grid.setItems(listaProductos);
//            return;
//        }
//
//        String texto = filtro.trim().toLowerCase();
//
//        List<Producto> filtrados = listaProductos.stream()
//                .filter(p -> p.getNombre().toLowerCase().contains(texto)
//                        || p.getId().toLowerCase().contains(texto))
//                .collect(Collectors.toList());
//
//        grid.setItems(filtrados);
//    }
}
