package com.almacenesgallego.primeraVersion.service;

import com.almacenesgallego.primeraVersion.service.filtros.FiltroProductos;
import com.almacenesgallego.primeraVersion.service.filtros.FiltroProductosFactory;
import com.almacenesgallego.primeraVersion.service.filtros.FiltroProductosPeñaSanta;
import com.almacenesgallego.primeraVersion.service.filtros.FiltroProductosTostados;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcesadorDocumento {

    private final ITesseract tesseract;
    private final ObjectMapper mapper;
    private final FiltroProductosFactory filtroProductosFactory;

    @Value("${tesseract.dpi:300}")
    private int dpi;

    public List<ProductoAlbaran> procesar(File archivoPDF, Long proveedorId) {

        List<ProductoAlbaran> todosPro = new ArrayList<>();;
//        FiltroProductosTostados.limpiarProductos();
//        FiltroProductosPeñaSanta.limpiarProductos();
        FiltroProductos filtro = filtroProductosFactory.getFiltro(proveedorId);

        try (PDDocument document = PDDocument.load(archivoPDF)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                System.out.println("Procesando página " + (page + 1));

                // 1️⃣ Renderizar página y hacer OCR
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi);
                String textoPagina = tesseract.doOCR(image);

                // 2️⃣ Filtrar directamente en memoria
//                String textoFiltrado = filtro.filtrarProductos(textoPagina);
                List<ProductoAlbaran> productosPagina = filtro.filtrarProductos(textoPagina);

                // 3️⃣ Guardar ambos (si realmente los necesitas)
//                String textoFiltrado = generarTxtProductos(productosPagina);
//                guardarResultado(generarNombreSalida(archivoPDF, page + 1, false), textoPagina);
//                guardarResultado(generarNombreSalida(archivoPDF, page + 1, true), textoFiltrado);

                todosPro.addAll(productosPagina);
            }

//            todosPro = FiltroProductosTostados.getTodosLosProductos();
//            todosPro = FiltroProductosPeñaSanta.getTodosLosProductos();


            File jsonFinal = new File(generarNombreJSON(archivoPDF));
            guardarJSON(jsonFinal, todosPro);

        } catch (IOException | TesseractException e) {
            e.printStackTrace();
        }
        return todosPro;
    }

    private String generarNombreSalida(File archivoPDF, int numeroPagina, boolean filtrado) {
        String outputDir = "C:\\Users\\pasan\\proyectos\\primeraVersion\\temptxt"; // <- escapa las barras
        String baseName = archivoPDF.getName().replaceFirst("[.][^.]+$", ""); // nombre sin extensión
        String fechaHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"));

        return outputDir + File.separator + baseName + "_pag" + numeroPagina + "_" + fechaHora +
                (filtrado ? "_filtrado.txt" : ".txt");
    }

    private String generarNombreJSON(File archivoPDF) {
        String outputDir = "C:\\Users\\pasan\\proyectos\\primeraVersion\\temptxt"; // directorio de salida
        String baseName = archivoPDF.getName().replaceFirst("[.][^.]+$", ""); // nombre sin extensión
        String fechaHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"));

        return outputDir + File.separator + baseName + "_productos_" + fechaHora + ".json";
    }

    private void guardarResultado(String nombreArchivo, String contenido) throws IOException {
        Path outputPath = Path.of(nombreArchivo);
        Files.write(outputPath, contenido.getBytes());
    }

    public void guardarJSON(File salidaJson, List<ProductoAlbaran> productos) throws IOException {
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        writer.writeValue(salidaJson, productos);
        System.out.println("JSON global guardado en: " + salidaJson.getAbsolutePath());
    }

    /**
     * Genera un String único con todos los productos listados
     */
    private static String generarTxtProductos(List<ProductoAlbaran> productos) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < productos.size(); i++) {
            ProductoAlbaran p = productos.get(i);

            out.append(p.codigo() == null ? "" : p.codigo())
                    .append(" | ")
                    .append(p.descripcion() == null ? "" : p.descripcion());

            if (p.cantidad() != null) {
                out.append(" ").append(formatCantidad(p.cantidad()));
            }

            if (p.fechaCaducidad() != null) {
                out.append(" Fecha Caducidad ").append(p.fechaCaducidad().format(df));
            }

            if (p.numeroLote() != null && !p.numeroLote().isBlank()) {
                out.append(" Lote ").append(p.numeroLote());
            }

            if (i < productos.size() - 1) out.append("\n");
        }

        return out.toString();
    }

    private static String formatCantidad(BigDecimal cantidad) {
        try {
            BigDecimal cleaned = cantidad.stripTrailingZeros();
            int scale = cleaned.scale();
            if (scale <= 0) {
                return cleaned.toBigInteger().toString();
            } else {
                return cleaned.toPlainString();
            }
        } catch (ArithmeticException ex) {
            return cantidad.toPlainString();
        }
    }
}
