package com.almacenesgallego.primeraVersion.view;

import com.almacenesgallego.primeraVersion.model.Producto;
import com.almacenesgallego.primeraVersion.model.StockLote;
import com.almacenesgallego.primeraVersion.view.components.BuscadorGenerico;
import com.almacenesgallego.primeraVersion.view.components.EditableGrid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Route(value = "stock", layout = MainView.class)
@PageTitle("Stock de Lotes")
public class StockLoteView extends VerticalLayout {

    private final WebClient webClient;
    private List<StockLote> listaStock;

    // === Grid editable ===
    private final EditableGrid<StockLote> grid = new EditableGrid<>(StockLote.class) {

        @Override
        protected void buildEditForm(FormLayout form, Binder<StockLote> binder, StockLote item) {
            TextField productoField = new TextField("Producto");
            productoField.setValue(item.getProducto() != null ? item.getProducto().getNombre() : "");
            productoField.setReadOnly(true);

            IntegerField cantidadField = new IntegerField("Cantidad");
            cantidadField.setValue(item.getCantidad() != null ? item.getCantidad() : 0);
            binder.forField(cantidadField)
                    .asRequired("La cantidad es obligatoria")
                    .withValidator(v -> v != null && v > 0, "Debe ser mayor que 0")
                    .bind(StockLote::getCantidad, StockLote::setCantidad);

            DatePicker fechaCaducidad = new DatePicker("Fecha Caducidad");
            binder.forField(fechaCaducidad)
                    .bind(StockLote::getFechaCaducidad, StockLote::setFechaCaducidad);

            TextField numeroLote = new TextField("Número de Lote");
            binder.forField(numeroLote)
                    .asRequired("El número de lote es obligatorio")
                    .bind(StockLote::getNumeroLote, StockLote::setNumeroLote);

            form.add(productoField, cantidadField, fechaCaducidad, numeroLote);

            binder.readBean(item);
        }

        @Override
        protected void onSave(StockLote lote) {
            try {
                if (lote.getId() == null) {
                    webClient.post()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(lote)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .block();
                    Notification.show("✅ Lote creado correctamente");
                } else {
                    webClient.put()
                            .uri("/{id}", lote.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(lote)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .block();
                    Notification.show("✅ Lote actualizado correctamente");
                }
                cargarDatos();
            } catch (Exception e) {
                Notification.show("Error al guardar: " + e.getMessage());
            }
        }
    };

    private final BuscadorGenerico<StockLote> buscador;

    public StockLoteView() {
        webClient = WebClient.create("http://localhost:8080/api/stocklotes");

        // === Configurar Grid ===
        configurarGrid();

        // === Buscador ===
        buscador = new BuscadorGenerico<>(
                "Buscar lote",
                grid,
                listaStock,
                (lote, texto) ->
                        (lote.getNumeroLote() != null && lote.getNumeroLote().toLowerCase().contains(texto))
                                || (lote.getProducto() != null && lote.getProducto().getNombre().toLowerCase().contains(texto))
        );

        // === Botones ===
        HorizontalLayout botonesLayout = configurarBotones();

        // === Layout general ===
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        add(buscador, grid, botonesLayout);
        expand(grid);

        cargarDatos();
    }

    private void configurarGrid() {
        grid.removeAllColumns();

        grid.addTextColumn(StockLote::getId, "ID");
        grid.addTextColumn(sl -> sl.getProducto() != null ? sl.getProducto().getNombre() : "", "Producto");
        grid.addTextColumn(StockLote::getCantidad, "Cantidad");
        grid.addTextColumn(StockLote::getFechaCaducidad, "Fecha Caducidad");
        grid.addTextColumn(StockLote::getNumeroLote, "Número de Lote");

        grid.addAccionesColumn(this::eliminarLote);
        grid.setSizeFull();
    }

    private HorizontalLayout configurarBotones() {
        Button btnNuevo = new Button("Añadir");
        Button btnEliminar = new Button("Eliminar seleccionado");

        btnNuevo.addClickListener(e -> mostrarDialogoNuevoStockLote());
//        btnNuevo.addClickListener(e -> grid.editarItem(new StockLote()));

        btnEliminar.addClickListener(e -> {
            StockLote seleccionado = grid.asSingleSelect().getValue();
            if (seleccionado != null) eliminarLote(seleccionado);
            else Notification.show("Selecciona un lote para eliminar.");
        });

        HorizontalLayout layout = new HorizontalLayout(btnNuevo, btnEliminar);
        layout.setWidthFull();
        layout.setJustifyContentMode(JustifyContentMode.END);
        layout.setPadding(true);
        return layout;
    }

    private void mostrarDialogoNuevoStockLote() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Añadir nuevo lote de stock");

        // === Campos ===
        ComboBox<Producto> productoCombo = new ComboBox<>("Producto");
        productoCombo.setItemLabelGenerator(p -> p.getId() + " - " + p.getNombre());
        productoCombo.setWidthFull();

        // Cargar lista de productos desde el backend
        List<Producto> productos = webClient.get()
                .uri("http://localhost:8080/api/productos")
                .retrieve()
                .bodyToFlux(Producto.class)
                .collectList()
                .block();
        productoCombo.setItems(productos);

        TextField numeroLoteField = new TextField("Número de lote");
        numeroLoteField.setWidthFull();

        NumberField cantidadField = new NumberField("Cantidad");
        cantidadField.setWidthFull();
        cantidadField.setMin(0);

        DatePicker fechaCaducidadPicker = new DatePicker("Fecha de caducidad");
        fechaCaducidadPicker.setWidthFull();

        // === Botones ===
        Button guardar = new Button("Guardar", event -> {
            if (productoCombo.isEmpty() || cantidadField.isEmpty() || numeroLoteField.isEmpty() || fechaCaducidadPicker.isEmpty()) {
                Notification.show("Completa todos los campos antes de guardar.");
                return;
            }

            try {
                StockLote nuevo = new StockLote();
                nuevo.setProducto(productoCombo.getValue());
                nuevo.setNumeroLote(numeroLoteField.getValue());
                nuevo.setCantidad(cantidadField.getValue().intValue());
                nuevo.setFechaCaducidad(fechaCaducidadPicker.getValue());

                webClient.post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(nuevo)
                        .retrieve()
                        .bodyToMono(StockLote.class)
                        .block();

                Notification.show("✅ Lote añadido correctamente");
                dialog.close();
                cargarDatos(); // refresca el grid
            } catch (Exception ex) {
                Notification.show("Error al guardar el lote: " + ex.getMessage());
            }
        });

        Button cancelar = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout botones = new HorizontalLayout(guardar, cancelar);
        botones.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout contenido = new VerticalLayout(
                productoCombo,
                numeroLoteField,
                cantidadField,
                fechaCaducidadPicker,
                botones
        );
        contenido.setPadding(false);
        contenido.setSpacing(true);

        dialog.add(contenido);
        dialog.open();
    }

    private void cargarDatos() {
        try {
            listaStock = webClient.get()
                    .retrieve()
                    .bodyToFlux(StockLote.class)
                    .collectList()
                    .block();

            grid.setItems(listaStock);
            if (buscador != null) buscador.setListaOriginal(listaStock);
        } catch (Exception e) {
            Notification.show("Error cargando lotes: " + e.getMessage());
        }
    }

    private void eliminarLote(StockLote lote) {
        try {
            webClient.delete()
                    .uri("/{id}", lote.getId())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            Notification.show("🗑️ Lote eliminado correctamente");
            cargarDatos();
        } catch (Exception e) {
            Notification.show("Error al eliminar: " + e.getMessage());
        }
    }
}
