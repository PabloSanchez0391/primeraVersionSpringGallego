package com.almacenesgallego.primeraVersion.view.components;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.grid.Grid;
import com.almacenesgallego.primeraVersion.model.Producto;

import java.util.List;
import java.util.stream.Collectors;

public class BuscadorProductos extends TextField {

    private final Grid<Producto> grid;
    private List<Producto> listaProductos;

    public BuscadorProductos(Grid<Producto> grid, List<Producto> listaProductos) {
        this.grid = grid;
        this.listaProductos = listaProductos;

        setLabel("Buscar producto");
        setPlaceholder("Escribe un nombre o ID...");
        setClearButtonVisible(true);
        setWidth("300px");
        setValueChangeMode(ValueChangeMode.EAGER);
        addValueChangeListener(event -> filtrar(event.getValue()));
    }

    /** Permite refrescar la lista en caso de recargar datos desde el backend */
    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
        filtrar(getValue()); // re-aplica el filtro actual
    }

    private void filtrar(String filtro) {
        if (listaProductos == null || listaProductos.isEmpty()) return;

        if (filtro == null || filtro.trim().isEmpty()) {
            grid.setItems(listaProductos);
            return;
        }

        String texto = filtro.trim().toLowerCase();

        List<Producto> filtrados = listaProductos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(texto)
                        || p.getId().toLowerCase().contains(texto))
                .collect(Collectors.toList());

        grid.setItems(filtrados);
    }
}
