package com.almacenesgallego.primeraVersion.config;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class TesseractConfig {

    @Value("${tesseract.language:spa}")
    private String idiomaOCR;

    @Bean
    public ITesseract tesseract() {
        Tesseract tesseract = new Tesseract();

        // 📌 Detecta si estamos en Docker o en local
        String dataPath;
        File dockerTessdata = new File("/app/tessdata");
        if (dockerTessdata.exists()) {
            dataPath = dockerTessdata.getAbsolutePath();
        } else {
            // fallback local (IDE)
            dataPath = "src/main/resources/tessdata";
        }

        tesseract.setDatapath(dataPath);
        tesseract.setLanguage(idiomaOCR);

        System.out.println("✅ Tesseract inicializado con:");
        System.out.println("   Path: " + dataPath);
        System.out.println("   Idioma: " + idiomaOCR);

        return tesseract;
    }
}
