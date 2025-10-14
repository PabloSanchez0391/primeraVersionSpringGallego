package com.almacenesgallego.primeraVersion.config;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

@Configuration
public class TesseractConfig {

    @Value("${tesseract.data-path}")
    private Resource tessdataResource;

    @Value("${tesseract.language:spa}")
    private String idiomaOCR;

    @Bean
    public ITesseract tesseract() throws IOException {
        Tesseract tesseract = new Tesseract();

        // 📦 Localiza la carpeta tessdata en el classpath (dentro de resources)
        File tessDataFolder = tessdataResource.getFile();
        tesseract.setDatapath(tessDataFolder.getAbsolutePath());
        tesseract.setLanguage(idiomaOCR);

        System.out.println("✅ Tesseract inicializado con:");
        System.out.println("   Path: " + tessDataFolder.getAbsolutePath());
        System.out.println("   Idioma: " + idiomaOCR);

        return tesseract;
    }
}
