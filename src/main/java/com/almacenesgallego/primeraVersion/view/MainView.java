package com.almacenesgallego.primeraVersion.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.stream.Stream;

@Route("")
public class MainView extends AppLayout {

    private VerticalLayout contentArea;
    private boolean isMobile;

    public MainView() {
        WebBrowser browser = VaadinSession.getCurrent().getBrowser();
        isMobile = browser.isAndroid() || browser.isIPhone();
//        isMobile = true;

        // Crear header y menú
//        HorizontalLayout header = buildHeader();
//        HorizontalLayout header = new HorizontalLayout(new Span("Menú"));
        buildMenu();

        // Configuración según dispositivo
        if (isMobile) {
            if (false){
                setPrimarySection(Section.DRAWER);
//                addToNavbar(true, header);   // true -> mostrar botón hamburguesa
                setDrawerOpened(false);
            }

            createHeaderMobile();
//            createDrawer();

        } else {
            buildHeaderPC();
            setPrimarySection(Section.NAVBAR);
//            addToNavbar(header);
            setDrawerOpened(true);
        }








        createContent();
    }

    private void createHeaderMobile() {
        H1 logo = new H1("Almacenes Gallego");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM
        );

        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().getStyle().set("margin", "0.5rem");

        addToNavbar(toggle, logo);
    }

    private void createDrawer() {
        RouterLink homeLink = new RouterLink("Inicio", StockLoteView.class);
        RouterLink settingsLink = new RouterLink("Configuración", ProveedorView.class);

        VerticalLayout menuLayout = new VerticalLayout(homeLink, settingsLink);
        menuLayout.setPadding(false);
        menuLayout.setSpacing(false);

//        addToDrawer(menuLayout);
    }



    private void buildHeaderPC() {
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
        header.setWidthFull();
        header.getStyle()
                .set("background-color", "#ecf0f1")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
                .set("box-sizing", "border-box");

        if (isMobile) {
            // Deja espacio para el botón de hamburguesa
            header.getStyle().set("padding-left", "3.5rem");
            // Asegura que no tape el botón
            header.getStyle().set("position", "relative").set("z-index", "0");
        }

        addToNavbar(header);
    }

    private void buildMenuViejo() {
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

        if (isMobile) {
            menu.getChildren().forEach(component -> {
                if (component instanceof Button button) {
                    button.addClickListener(e -> setDrawerOpened(false));
                }
            });
        }

        addToDrawer(menu);
    }

    private void buildMenu() {
        RouterLink stockLink = new RouterLink("Stock", StockLoteView.class);
        RouterLink subirDocLink = new RouterLink("Subir Documento", SubirDocumentoView.class);
        RouterLink productosLink = new RouterLink("Productos", ProductoView.class);
        RouterLink proveedorLink = new RouterLink("Proveedores", ProveedorView.class);

        // Aplica estilos tipo botón a todos los links
        Stream.of(stockLink, subirDocLink, productosLink, proveedorLink).forEach(link -> {
            link.getStyle()
                    .set("display", "block")
                    .set("width", "100%")
                    .set("padding", "10px 16px")
                    .set("margin-bottom", "10px")
                    .set("border", "none")
                    .set("border-radius", "5px")
                    .set("background-color", "#3498db")
                    .set("color", "white")
                    .set("text-align", "center")
                    .set("cursor", "pointer")
                    .set("text-decoration", "none")
                    .set("font-weight", "500")
                    .set("transition", "background-color 0.2s ease");

            // efecto hover
            link.getElement().addEventListener("mouseover", e ->
                    link.getStyle().set("background-color", "#2980b9"));
            link.getElement().addEventListener("mouseout", e ->
                    link.getStyle().set("background-color", "#3498db"));
        });

        VerticalLayout menu = new VerticalLayout(stockLink, subirDocLink, productosLink, proveedorLink);
        menu.setPadding(true);
        menu.setSpacing(true);
        menu.setWidth("200px");
        menu.getStyle()
                .set("background-color", "#ecf0f1")
                .set("height", "100%")
                .set("border-radius", "0 5px 5px 0")
                .set("padding", "15px");

        // En móvil: cerrar el drawer al tocar
        if (isMobile) {
            menu.getChildren().forEach(component ->
                    component.getElement().addEventListener("click", e -> setDrawerOpened(false)));
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

        // Nuevo: mostrar tipo de dispositivo
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
