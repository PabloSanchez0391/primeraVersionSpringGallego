package com.almacenesgallego.primeraVersion.view.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Buscador genérico reutilizable para cualquier tipo de entidad.
 *
 * @param <T> Tipo de los elementos que se mostrarán en el grid.
 */
public class BuscadorGenerico<T> extends TextField {

    private final Grid<T> grid;
    private List<T> listaOriginal;
    private final BiPredicate<T, String> filtro; // lógica personalizada de filtrado

    /**
     * Constructor principal.
     *
     * @param label         Etiqueta del campo de texto (por ejemplo: "Buscar producto")
     * @param grid          Grid donde se mostrarán los resultados filtrados
     * @param listaOriginal Lista completa de elementos
     * @param filtro        Función que define si un elemento coincide con el texto introducido
     */
    public BuscadorGenerico(String label, Grid<T> grid, List<T> listaOriginal, BiPredicate<T, String> filtro) {
        this.grid = grid;
        this.listaOriginal = listaOriginal;
        this.filtro = filtro;

        setLabel(label);
        setPlaceholder("Escribe para buscar...");
        setClearButtonVisible(true);
        setWidth("300px");
        setValueChangeMode(ValueChangeMode.EAGER);

        addValueChangeListener(event -> filtrar(event.getValue()));
    }

    /**
     * Permite refrescar la lista cuando se recargan los datos desde el backend.
     *
     * @param nuevaLista Nueva lista de elementos completa
     */
    public void setListaOriginal(List<T> nuevaLista) {
        this.listaOriginal = nuevaLista;
        filtrar(getValue()); // Re-aplica el filtro actual para mantener consistencia
    }

    /**
     * Filtra los elementos del grid según el texto introducido.
     *
     * @param texto Texto introducido en el campo de búsqueda
     */
    private void filtrar(String texto) {
        if (listaOriginal == null || listaOriginal.isEmpty()) {
            return;
        }

        if (texto == null || texto.trim().isEmpty()) {
            grid.setItems(listaOriginal);
            return;
        }

        String valor = texto.trim().toLowerCase();

        List<T> filtrados = listaOriginal.stream()
                .filter(item -> filtro.test(item, valor))
                .collect(Collectors.toList());

        grid.setItems(filtrados);
    }
}
