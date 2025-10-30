package com.almacenesgallego.primeraVersion.controller;

import com.almacenesgallego.primeraVersion.model.ReferenciaProveedor;
import com.almacenesgallego.primeraVersion.service.ReferenciaProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/referencias-proveedor")
@RequiredArgsConstructor
public class ReferenciaProveedorController {

    private final ReferenciaProveedorService referenciaProveedorService;

    @PostMapping
    public ResponseEntity<ReferenciaProveedor> guardar(@RequestBody ReferenciaProveedor ref) {
        ReferenciaProveedor guardada = referenciaProveedorService.guardar(ref);
        return ResponseEntity.ok(guardada);
    }

    @GetMapping
    public ResponseEntity<List<ReferenciaProveedor>> obtenerTodas() {
        return ResponseEntity.ok(referenciaProveedorService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReferenciaProveedor> obtenerPorId(@PathVariable Integer id) {
        return referenciaProveedorService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        referenciaProveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
