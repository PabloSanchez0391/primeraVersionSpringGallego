package com.almacenesgallego.primeraVersion.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayout.Section;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;

@Route("")
public class MainView extends AppLayout {

    private VerticalLayout contentArea;
    private boolean isMobile;

    public MainView() {
        WebBrowser browser = VaadinSession.getCurrent().getBrowser();
        isMobile = browser.isAndroid() || browser.isIPhone();

        // Configurar secciones
        if (isMobile) {
            setPrimarySection(Section.DRAWER);
        } else {
            setPrimarySection(Section.NAVBAR);
        }

        // Crear header y menú
        HorizontalLayout header = buildHeader();
        buildMenu();

        // Añadir header
        // En móvil => addToNavbar(true, header) para mostrar hamburguesa
        if (isMobile) {
            addToNavbar(true, header);
            setDrawerOpened(false);
        } else {
            addToNavbar(header);
            setDrawerOpened(true);
        }

        createContent();
    }

    private HorizontalLayout buildHeader() {
        H1 title = new H1("Almacenes Gallego");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("color", "#2c3e50");

        HorizontalLayout header = new HorizontalLayout(title);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.getStyle()
                .set("background-color", "#ecf0f1")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
                .set("padding", "0.5rem 1rem")
                .set("box-sizing", "border-box");

        if (isMobile) {
            // Deja hueco a la izquierda para el botón hamburguesa
            header.getStyle().set("padding-left", "3.5rem");
        }

        return header;
    }

    private void buildMenu() {
        Button stockButton = new Button("Stock", e -> getUI().ifPresent(ui -> ui.navigate(StockLoteView.class)));
        Button subirDocButton = new Button("Subir Documento", e -> getUI().ifPresent(ui -> ui.navigate(SubirDocumentoView.class)));
        Button productosButton = new Button("Productos", e -> getUI().ifPresent(ui -> ui.navigate(ProductoView.class)));
        Button proveedorButton = new Button("Proveedores", e -> getUI().ifPresent(ui -> ui.navigate(ProveedorView.class)));

        VerticalLayout menu = new VerticalLayout(stockButton, subirDocButton, productosButton, proveedorButton);
        menu.setPadding(true);
        menu.setSpacing(true);
        menu.setWidth("200px");
        menu.getStyle()
                .set("background-color", "#ecf0f1")
                .set("height", "100%");

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

        Span deviceInfo = new Span(isMobile ? "Dispositivo detectado: MÓVIL" : "Dispositivo detectado: ORDENADOR");
        deviceInfo.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", isMobile ? "green" : "blue")
                .set("margin-top", "10px");

        contentArea.add(welcomeText, deviceInfo);

        setContent(contentArea);
    }

    public void setMainContent(VerticalLayout newContent) {
        contentArea.removeAll();
        contentArea.add(newContent);
    }
}
