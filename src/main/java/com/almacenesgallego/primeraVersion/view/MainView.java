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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;

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

        // 📦 VerticalLayout para colocar título y subtítulo uno debajo del otro
        VerticalLayout titleLayout = new VerticalLayout(title, subtitle);
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);
        titleLayout.setAlignItems(Alignment.START);

        // 📏 HorizontalLayout para que el header sea una barra completa
        HorizontalLayout header = new HorizontalLayout(titleLayout);
        header.setAlignItems(Alignment.CENTER);
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.getStyle()
                .set("background-color", "#ecf0f1")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");

        // ✅ El `true` mantiene el botón hamburguesa visible en móvil
        addToNavbar(true, header);
    }


    private void createMenu() {
        Button stockButton = new Button("Stock", e -> getUI().ifPresent(ui -> ui.navigate(StockLoteView.class)));
        Button subirDocButton = new Button("Subir Documento", e -> getUI().ifPresent(ui -> ui.navigate(SubirDocumentoView.class)));
        Button productosButton = new Button("Productos", e -> getUI().ifPresent(ui -> ui.navigate(ProductoView.class)));
        Button proveedorButton = new Button("Proveedores", e -> getUI().ifPresent(ui -> ui.navigate(ProveedorView.class)));

        stockButton.getStyle().set("width", "100%").set("margin-bottom", "10px");
        subirDocButton.getStyle().set("width", "100%");
        productosButton.getStyle().set("width", "100%");
        proveedorButton.getStyle().set("width", "100%");

        VerticalLayout menu = new VerticalLayout(stockButton, subirDocButton, productosButton, proveedorButton);
        menu.setPadding(true);
        menu.setSpacing(true);
        menu.setWidth("200px");
        menu.getStyle()
                .set("background-color", "#ecf0f1")
                .set("height", "100%")
                .set("border-radius", "0 5px 5px 0");

        // 💡 Cerrar el drawer solo en móvil
        WebBrowser browser = VaadinSession.getCurrent().getBrowser();
        boolean isMobile = browser.isAndroid() || browser.isIPhone();

        if (isMobile) {
            menu.getChildren().forEach(component -> {
                if (component instanceof Button button) {
                    button.addClickListener(e -> setDrawerOpened(false));
                }
            });
        }

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

    public void setMainContent(VerticalLayout newContent) {
        contentArea.removeAll();
        contentArea.add(newContent);
    }
}
