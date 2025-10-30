package com.almacenesgallego.primeraVersion.controller;

import com.almacenesgallego.primeraVersion.dto.ProductoDTO;
import com.almacenesgallego.primeraVersion.mapper.ProductoMapper;
import com.almacenesgallego.primeraVersion.model.Producto;
import com.almacenesgallego.primeraVersion.repository.ProductoRepository;
import com.almacenesgallego.primeraVersion.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping("/importar")
    public void importarCSV(@RequestParam("file") MultipartFile file) throws IOException {
        // Guardar temporalmente o pasar InputStream a ProductoService.importarCSV
        productoService.importarCSV(file.getInputStream());
    }

//    @GetMapping
//    public ResponseEntity<List<ProductoDTO>> obtenerTodos() {
//        return ResponseEntity.ok(productoService.obtenerTodos());
//    }

    @GetMapping
    public ResponseEntity<List<Producto>> findAll() {
        return ResponseEntity.ok(productoService.findAll());
    }

//    @GetMapping
//    public ResponseEntity<List<ProductoDTO>> obtenerTodos() {
//        return ResponseEntity.ok(productoService.obtenerTodos());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> findById(@PathVariable String id) {
        return productoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> save(@RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.save(producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
