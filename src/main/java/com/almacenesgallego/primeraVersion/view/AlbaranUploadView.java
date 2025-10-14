//package com.almacenesgallego.primeraVersion.view;
//
//import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.html.H3;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.upload.Upload;
//
//import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
//import com.vaadin.flow.router.Route;
//import com.vaadin.flow.router.PageTitle;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.http.MediaType;
//import org.springframework.core.io.ByteArrayResource;
//
//import java.io.InputStream;
//import java.util.List;
//
//@Route("albaran")
//@PageTitle("Subir albarán")
//public class AlbaranUploadView extends VerticalLayout {
//
//    private final Grid<ProductoAlbaran> grid = new Grid<>(ProductoAlbaran.class, false);
//    private List<ProductoAlbaran> productosExtraidos;
//
//    public AlbaranUploadView() {
//        setPadding(true);
//        setSpacing(true);
//        setWidthFull();
//
//        add(new H3("Subir albarán en PDF"));
//
//        MemoryBuffer buffer = new MemoryBuffer();
//        Upload upload = new Upload(buffer);
//        upload.setAcceptedFileTypes("application/pdf");
//
//        upload.addSucceededListener(event -> {
//            try (InputStream inputStream = buffer.getInputStream()) {
//                byte[] fileBytes = inputStream.readAllBytes();
//                productosExtraidos = enviarArchivoAlBackend(fileBytes);
//                if (productosExtraidos != null && !productosExtraidos.isEmpty()) {
//                    mostrarTabla(productosExtraidos);
//                } else {
//                    Notification.show("No se detectaron productos en el albarán.");
//                }
//            } catch (Exception e) {
//                Notification.show("Error al procesar el archivo: " + e.getMessage());
//                e.printStackTrace();
//            }
//        });
//
//        add(upload, grid);
//    }
//
//    private List<ProductoAlbaran> enviarArchivoAlBackend(byte[] fileBytes) {
//        return WebClient.create("http://localhost:8080")
//                .post()
//                .uri("/api/albaran/upload")
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .bodyValue(new org.springframework.util.LinkedMultiValueMap<>() {{
//                    add("file", new ByteArrayResource(fileBytes) {
//                        @Override
//                        public String getFilename() {
//                            return "albaran.pdf";
//                        }
//                    });
//                }})
//                .retrieve()
//                .bodyToFlux(ProductoAlbaran.class)
//                .collectList()
//                .block();
//    }
//
//    private void mostrarTabla(List<ProductoAlbaran> productos) {
//        grid.removeAllColumns();
//        grid.addColumn(ProductoAlbaran::codigo).setHeader("Código");
//        grid.addColumn(ProductoAlbaran::descripcion).setHeader("Descripción");
//        grid.addColumn(ProductoAlbaran::cantidad).setHeader("Cantidad");
//        grid.addColumn(ProductoAlbaran::fechaCaducidad).setHeader("Caducidad");
//        grid.addColumn(ProductoAlbaran::numeroLote).setHeader("Nº Lote");
//        grid.setItems(productos);
//
//        Button guardarBtn = new Button("💾 Guardar en base de datos", e -> guardarProductos());
//        add(guardarBtn);
//    }
//
//    private void guardarProductos() {
//        if (productosExtraidos == null || productosExtraidos.isEmpty()) {
//            Notification.show("No hay productos para guardar.");
//            return;
//        }
//
//        WebClient.create("http://localhost:8080")
//                .post()
//                .uri("/api/stocklote/guardar")
//                .bodyValue(productosExtraidos)
//                .retrieve()
//                .toBodilessEntity()
//                .block();
//
//        Notification.show("Productos guardados correctamente ✅");
//    }
//}
