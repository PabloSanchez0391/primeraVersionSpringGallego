package com.almacenesgallego.primeraVersion.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayout.Section;
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
    private boolean isMobile;

    public MainView() {
        WebBrowser browser = VaadinSession.getCurrent().getBrowser();
        isMobile = browser.isAndroid() || browser.isIPhone();

        // Primero creamos header y menu (header necesita saber isMobile para mostrar/hacer hueco al toggle)
        HorizontalLayout header = buildHeader();
        buildMenu();

        // Comportamiento por dispositivo
        if (isMobile) {
            // Drawer superpuesto, navbar muestra toggle y header en navbar
            setPrimarySection(Section.DRAWER);
            addToNavbar(true, header);   // true -> mostrar botón hamburguesa
            setDrawerOpened(false);      // cerrado por defecto en móvil
        } else {
            // Escritorio: navbar (header) es primaria y ocupa todo el ancho; drawer fijo a la izquierda
            setPrimarySection(Section.NAVBAR);
            addToNavbar(header);         // sin toggle, header ocupa todo el ancho
            setDrawerOpened(true);       // abierto por defecto en escritorio
        }

        createContent();
    }

    // Construye el header y lo devuelve para añadirlo después según el dispositivo
    private HorizontalLayout buildHeader() {
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

        VerticalLayout titleLayout = new VerticalLayout(title, subtitle);
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);
        titleLayout.setAlignItems(Alignment.START);

        HorizontalLayout header = new HorizontalLayout(titleLayout);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.setPadding(true);
        header.setSpacing(true);
        header.setWidthFull(); // importante: ocupa todo el ancho
        header.getStyle()
                .set("background-color", "#ecf0f1")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
                .set("box-sizing", "border-box");

        // En móvil dejamos espacio al icono hamburguesa para que no quede tapado
        if (isMobile) {
            header.expand(titleLayout);
        }

        return header;
    }

    private void buildMenu() {
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

        // En móvil: cerrar drawer al pulsar un item
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
