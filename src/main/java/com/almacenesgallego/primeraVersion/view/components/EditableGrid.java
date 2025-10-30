package com.almacenesgallego.primeraVersion.view.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.ValueProvider;

import java.util.function.Consumer;

public abstract class EditableGrid<T> extends Grid<T> {

    protected final Binder<T> binder;
    private final Dialog editDialog;
    private final FormLayout formLayout;
    private T currentItem;

    public EditableGrid(Class<T> beanType) {
        super(beanType);
        this.binder = new Binder<>(beanType);
        this.formLayout = new FormLayout();
        this.editDialog = new Dialog();
        configurarBase();
        configurarDialogo();
    }

    /** Configuración general del grid */
    private void configurarBase() {
        this.setWidthFull();
        this.setAllRowsVisible(true);
        this.addClassName("editable-grid");
    }

    /** Configura el diálogo de edición */
    private void configurarDialogo() {
        editDialog.setHeaderTitle("Editar registro");
        editDialog.setModal(true);
        editDialog.setResizable(true);
        editDialog.setDraggable(true);

        Button btnGuardar = new Button("💾 Guardar", event -> {
            if (binder.writeBeanIfValid(currentItem)) {
                onSave(currentItem);
                editDialog.close();
                this.getDataProvider().refreshAll();
            }
        });

        Button btnCancelar = new Button("Cancelar", event -> editDialog.close());

        HorizontalLayout footer = new HorizontalLayout(btnGuardar, btnCancelar);
        footer.setSpacing(true);

        editDialog.add(formLayout);
        editDialog.getFooter().add(footer);
    }

    /**
     * Muestra el diálogo de edición con los campos que defina la subclase.
     */
    protected void openEditDialog(T item) {
        this.currentItem = item;
        this.formLayout.removeAll();
        this.binder.readBean(item);

        // Los campos los define la subclase
        buildEditForm(formLayout, binder, item);

        editDialog.open();
    }

    /**
     * Método abstracto para que las subclases construyan los campos del formulario.
     */
    protected abstract void buildEditForm(FormLayout form, Binder<T> binder, T item);

    /**
     * Acción que se ejecuta al guardar un registro.
     * (La vista concreta debe sobrescribir si quiere persistir datos.)
     */
    protected void onSave(T item) {
        // Por defecto no hace nada, la subclase puede sobreescribirlo
    }

    /**
     * Añade una columna de texto simple (no editable).
     */
    public <V> Grid.Column<T> addTextColumn(ValueProvider<T, V> getter, String header) {
        return this.addColumn(getter)
                .setHeader(header)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
    }

    /**
     * Añade columna con botones de acción: editar y eliminar.
     */
    public void addAccionesColumn(Consumer<T> onDelete) {
        this.addComponentColumn(item -> {
                    Button editar = new Button("✏️", e -> openEditDialog(item));
                    Button eliminar = new Button("🗑️", e -> onDelete.accept(item));

                    editar.getStyle().set("margin-right", "5px");
                    eliminar.getStyle().set("color", "red");

                    HorizontalLayout layout = new HorizontalLayout(editar, eliminar);
                    layout.setPadding(false);
                    layout.setSpacing(true);
                    return layout;
                }).setHeader("Acciones")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // doble clic para editar también
        this.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
    }

    /**
     * Muestra un mensaje si la lista está vacía.
     */
    public void mostrarMensajeVacio(String mensaje) {
        this.setItems();
        this.getElement().appendChild(new Span(mensaje).getElement());
    }

    /**
     * Permite abrir el diálogo de edición desde fuera (por ejemplo, para crear un nuevo registro).
     */
    public void editarItem(T item) {
        openEditDialog(item);
    }
}
