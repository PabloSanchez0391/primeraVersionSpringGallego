package com.almacenesgallego.primeraVersion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;
import jakarta.annotation.PostConstruct;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProcesadorDocumento {

    private final ITesseract tesseract;
    private final ObjectMapper mapper;

    @Value("${tesseract.dpi:300}")
    private int dpi;

    public ProcesadorDocumento(ITesseract tesseract, ObjectMapper mapper) {
        this.tesseract = tesseract;
        this.mapper = mapper;
    }

    public List<ProductoAlbaran> procesar(File archivoPDF) {
        List<ProductoAlbaran> todosPro = null;
        FiltroProductos.limpiarProductos();
        try (PDDocument document = PDDocument.load(archivoPDF)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                System.out.println("Procesando página " + (page + 1));

                // 1️⃣ Renderizar página y hacer OCR
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi);
                String textoPagina = tesseract.doOCR(image);

                // 2️⃣ Filtrar directamente en memoria
                String textoFiltrado = FiltroProductos.filtrarProductos(textoPagina);

                // 3️⃣ Guardar ambos (si realmente los necesitas)
//                guardarResultado(generarNombreSalida(archivoPDF, page + 1, false), textoPagina);
//                guardarResultado(generarNombreSalida(archivoPDF, page + 1, true), textoFiltrado);
            }

            todosPro = FiltroProductos.getTodosLosProductos();

//            File jsonFinal = new File(generarNombreJSON(archivoPDF));
//            guardarJSON(jsonFinal, todosPro);

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
}
