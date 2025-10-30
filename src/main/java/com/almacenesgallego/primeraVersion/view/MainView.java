package com.almacenesgallego.primeraVersion.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;

@Route("")
public class MainView extends AppLayout {

    private VerticalLayout contentArea;

    public MainView() {
        createHeader();
        createMenu();
        createContent();
    }

    private void createHeader() {
        H1 title = new H1("Almacenes Gallego");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("color", "#2c3e50");

        H3 subtitle = new H3("Gestión de Stock y Documentos");
        subtitle.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("color", "#34495e");

        VerticalLayout header = new VerticalLayout(title, subtitle);
        header.setPadding(true);
        header.setSpacing(false);
        header.setDefaultHorizontalComponentAlignment(Alignment.START);

        addToNavbar(header);
    }

    private void createMenu() {
        // Botones de navegación
        Button stockButton = new Button("Stock", e -> getUI().ifPresent(ui -> ui.navigate(StockLoteView.class)));
        Button subirDocButton = new Button("Subir Documento", e -> getUI().ifPresent(ui -> ui.navigate(SubirDocumentoView.class)));
        Button productosButton = new Button("Productos", e -> getUI().ifPresent(ui -> ui.navigate(ProductoView.class)));
        Button proveedorButton = new Button("Proveedores", e -> getUI().ifPresent(ui -> ui.navigate(ProveedorView.class)));

        // Estilo de los botones
        stockButton.getStyle().set("width", "100%").set("margin-bottom", "10px");
        subirDocButton.getStyle().set("width", "100%");
        productosButton.getStyle().set("width", "100%");
        proveedorButton.getStyle().set("width", "100%");

        // Layout del menú lateral
        VerticalLayout menu = new VerticalLayout(stockButton, subirDocButton, productosButton, proveedorButton);
        menu.setPadding(true);
        menu.setSpacing(true);
        menu.setWidth("200px");
        menu.getStyle()
                .set("background-color", "#ecf0f1")
                .set("height", "100%")
                .set("border-radius", "5px");

        addToDrawer(menu);
    }

    private void createContent() {
        contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        contentArea.setAlignItems(FlexComponent.Alignment.CENTER);
        contentArea.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Span welcomeText = new Span("Bienvenido a Almacenes Gallego. Selecciona una opción del menú.");
        welcomeText.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("color", "#34495e");

        contentArea.add(welcomeText);

        setContent(contentArea);
    }

    // Método para actualizar el contenido dinámicamente desde otras vistas
    public void setMainContent(VerticalLayout newContent) {
        contentArea.removeAll();
        contentArea.add(newContent);
    }
}
