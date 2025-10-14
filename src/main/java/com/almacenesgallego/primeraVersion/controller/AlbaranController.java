//package com.almacenesgallego.primeraVersion.controller;
//
//import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;
//import com.almacenesgallego.primeraVersion.service.AlbaranService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/albaran")
//public class AlbaranController {
//
//    private final AlbaranService albaranService;
//
//    public AlbaranController(AlbaranService albaranService) {
//        this.albaranService = albaranService;
//    }
//
//    @PostMapping("/upload")
//    public ResponseEntity<List<ProductoAlbaran>> uploadAlbaran(@RequestParam("file") MultipartFile file)
//            throws IOException {
//
//        // Guardamos el archivo temporalmente
//        File tempFile = File.createTempFile("albaran_", ".pdf");
//        file.transferTo(tempFile);
//
//        List<ProductoAlbaran> productos = albaranService.procesarAlbaran(tempFile);
//        return ResponseEntity.ok(productos);
//    }
//}
