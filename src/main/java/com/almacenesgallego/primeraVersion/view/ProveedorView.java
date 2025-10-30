package com.almacenesgallego.primeraVersion.view;

import com.almacenesgallego.primeraVersion.model.Proveedor;
import com.almacenesgallego.primeraVersion.view.components.BuscadorGenerico;
import com.almacenesgallego.primeraVersion.view.components.EditableGrid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Route(value = "proveedores", layout = MainView.class)
@PageTitle("Listado de Proveedores")
public class ProveedorView extends VerticalLayout {

    private final WebClient webClient;
    private final EditableGrid<Proveedor> grid = new EditableGrid<>(Proveedor.class) {

        @Override
        protected void buildEditForm(FormLayout form, Binder<Proveedor> binder, Proveedor item) {
            // === Campos del diálogo de edición ===
            TextField idField = new TextField("ID");
            idField.setReadOnly(true);

            TextField nombreField = new TextField("Nombre del proveedor");
            nombreField.setWidthFull();

            // === Binding ===
            binder.bind(idField, p -> String.valueOf(p.getId()), null);
            binder.bind(nombreField, Proveedor::getNombre, Proveedor::setNombre);

            form.add(idField, nombreField);

            binder.readBean(item);
        }

        @Override
        protected void onSave(Proveedor item) {
            // Guardar en backend
            try {
                webClient.put()
                        .uri("/{id}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(item)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();

                Notification.show("Proveedor actualizado: " + item.getNombre());
                cargarDatos();
            } catch (Exception e) {
                Notification.show("Error al actualizar: " + e.getMessage());
            }
        }
    };

//    private final TextField buscador = new TextField("Buscar proveedor");
    private final BuscadorGenerico<Proveedor> buscador;
    private List<Proveedor> listaProveedores;

    public ProveedorView() {
        webClient = WebClient.create("http://localhost:8080/api/proveedores");

        buscador = new BuscadorGenerico<>(
                "Buscar producto",
                grid,
                listaProveedores,
                (proveedor, texto) -> proveedor.getNombre().toLowerCase().contains(texto)
                        || String.valueOf(proveedor.getId()).contains(texto)
        );


//        configurarBuscador();
        configurarGrid();
        cargarDatos();

        HorizontalLayout botonesLayout = configurarBotones();

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

        grid.addTextColumn(Proveedor::getId, "ID");
        grid.addTextColumn(Proveedor::getNombre, "Nombre");

        // Columna de acciones con el diálogo de edición integrado
        grid.addAccionesColumn(this::eliminarProveedor);

        grid.setSizeFull();
    }

    private HorizontalLayout configurarBotones() {
        Button btnNuevo = new Button("Añadir", e -> mostrarDialogoNuevoProveedor());
        Button btnEliminar = new Button("Eliminar", e -> {
            Proveedor seleccionado = grid.asSingleSelect().getValue();
            if (seleccionado != null)
                eliminarProveedor(seleccionado);
            else
                Notification.show("Selecciona un proveedor para eliminar.");
        });

        HorizontalLayout layout = new HorizontalLayout(btnNuevo, btnEliminar);
        layout.setWidthFull();
        layout.setJustifyContentMode(JustifyContentMode.END);
        layout.setPadding(true);

        return layout;
    }

    private void mostrarDialogoNuevoProveedor() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Añadir nuevo proveedor");

        TextField nombreField = new TextField("Nombre del proveedor");
        nombreField.setWidthFull();

        Button btnGuardar = new Button("Guardar", event -> {
            String nombre = nombreField.getValue().trim();

            if (nombre.isEmpty()) {
                Notification.show("Por favor, introduce un nombre.");
                return;
            }

            try {
                Proveedor nuevoProveedor = new Proveedor();
                nuevoProveedor.setNombre(nombre);

                Proveedor guardado = webClient.post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(nuevoProveedor)
                        .retrieve()
                        .bodyToMono(Proveedor.class)
                        .block();

                if (guardado != null) {
                    Notification.show("Proveedor guardado: " + guardado.getNombre());
                    dialog.close();
                    cargarDatos();
                } else {
                    Notification.show("Error al guardar el proveedor.");
                }
            } catch (Exception ex) {
                Notification.show("Error al guardar: " + ex.getMessage());
            }
        });

        Button btnCancelar = new Button("Cancelar", event -> dialog.close());

        HorizontalLayout botones = new HorizontalLayout(btnGuardar, btnCancelar);
        botones.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout contenido = new VerticalLayout(nombreField, botones);
        contenido.setPadding(false);
        contenido.setSpacing(true);

        dialog.add(contenido);
        dialog.open();
    }

    private void cargarDatos() {
        try {
            listaProveedores = webClient.get()
                    .retrieve()
                    .bodyToFlux(Proveedor.class)
                    .collectList()
                    .block();

            grid.setItems(listaProveedores);
            buscador.setListaOriginal(listaProveedores);
        } catch (Exception e) {
            Notification.show("Error cargando proveedores: " + e.getMessage());
        }
    }

    private void eliminarProveedor(Proveedor proveedor) {
        try {
            webClient.delete()
                    .uri("/{id}", proveedor.getId())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            Notification.show("Proveedor eliminado: " + proveedor.getNombre());
            cargarDatos();
        } catch (Exception e) {
            Notification.show("Error al eliminar: " + e.getMessage());
        }
    }

//    private void filtrar(String filtro) {
//        if (listaProveedores == null || listaProveedores.isEmpty()) return;
//
//        if (filtro == null || filtro.trim().isEmpty()) {
//            grid.setItems(listaProveedores);
//            return;
//        }
//
//        String texto = filtro.trim().toLowerCase();
//
//        List<Proveedor> filtrados = listaProveedores.stream()
//                .filter(p -> p.getNombre().toLowerCase().contains(texto)
//                        || String.valueOf(p.getId()).contains(texto))
//                .collect(Collectors.toList());
//
//        grid.setItems(filtrados);
//    }
}
