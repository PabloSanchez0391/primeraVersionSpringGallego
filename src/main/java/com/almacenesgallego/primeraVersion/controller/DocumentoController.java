//package com.almacenesgallego.primeraVersion.controller;
//
//import com.almacenesgallego.primeraVersion.service.ProcesadorDocumento;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
//@RestController
//@RequestMapping("/api/documentos")
//@CrossOrigin(origins = "*")
//public class DocumentoController {
//
//    private final ProcesadorDocumento procesadorDocumento;
//
//    public DocumentoController(ProcesadorDocumento procesadorDocumento) {
//        this.procesadorDocumento = procesadorDocumento;
//    }
//
//    @PostMapping("/analyze")
//    public void analizar(@RequestBody byte[] contenido) throws IOException {
//        procesadorDocumento.procesar(contenido);
//    }
//}
