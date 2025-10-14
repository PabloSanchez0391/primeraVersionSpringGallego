package com.almacenesgallego.primeraVersion.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.almacenesgallego.primeraVersion.model.StockLote;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Route("")
public class StockLoteView extends VerticalLayout {

    private final WebClient webClient;
    private final Grid<StockLote> grid = new Grid<>(StockLote.class);

    public StockLoteView() {
        webClient = WebClient.create("http://localhost:8080/api/stocklotes");

        Button refreshBtn = new Button("Recargar", e -> cargarDatos());
        RouterLink subirDocLink = new RouterLink("Subir documento", SubirDocumentoView.class);

        HorizontalLayout topBar = new HorizontalLayout(refreshBtn, subirDocLink);
        add(topBar, grid);

        grid.removeAllColumns();
        grid.addColumn(sl -> sl.getId()).setHeader("ID");
        grid.addColumn(sl -> sl.getProducto() != null ? sl.getProducto().getNombre() : "").setHeader("Producto");
        grid.addColumn(StockLote::getCantidad).setHeader("Cantidad");
        grid.addColumn(StockLote::getFechaCaducidad).setHeader("Fecha Caducidad");
        grid.addColumn(StockLote::getNumeroLote).setHeader("Número de Lote");

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            List<StockLote> lista = webClient.get()
                    .retrieve()
                    .bodyToFlux(StockLote.class)
                    .collectList()
                    .block();

            grid.setItems(lista);
        } catch (Exception e) {
            Notification.show("Error cargando datos: " + e.getMessage());
        }
    }
}
