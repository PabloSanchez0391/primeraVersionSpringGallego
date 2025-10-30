package com.almacenesgallego.primeraVersion.controller;

import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;
import com.almacenesgallego.primeraVersion.service.ProcesadorDocumento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// TODO va a desuso
@RestController
@RequestMapping("/api/pdf")
public class PdfUploadController {

    @Autowired
    private ProcesadorDocumento procesadorDocumento;

    @PostMapping("/upload")
    public List<ProductoAlbaran> handlePdfUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("proveedorId") Long proveedorId
    ) {

        if (file.isEmpty()) {
            throw new RuntimeException("No se ha subido ningún archivo");
        }

        // Guardar temporalmente o procesar directamente
        try {
            Path tempFile = Files.createTempFile("albaran-", ".pdf");
            file.transferTo(tempFile.toFile());

            // El procesador devuelve una lista de productos detectados
            List<ProductoAlbaran> productosDetectados = procesadorDocumento.procesar(tempFile.toFile(), proveedorId);

            // Devolverlos como JSON
            return productosDetectados;

        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el archivo: " + e.getMessage(), e);
        }
    }
}

