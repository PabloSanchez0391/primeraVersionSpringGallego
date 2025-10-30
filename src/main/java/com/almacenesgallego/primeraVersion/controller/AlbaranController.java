package com.almacenesgallego.primeraVersion.controller;

import com.almacenesgallego.primeraVersion.model.Proveedor;
import com.almacenesgallego.primeraVersion.service.AlbaranService;
import com.almacenesgallego.primeraVersion.repository.ProveedorRepository;
import com.almacenesgallego.primeraVersion.wrapper.ProductoAlbaranEditable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albaranes")
@RequiredArgsConstructor
public class AlbaranController {

    private final AlbaranService albaranService;
    private final ProveedorRepository proveedorRepository;

    /**
     * Endpoint para guardar un albarán de un proveedor concreto.
     *
     * @param proveedorId ID del proveedor
     * @param productosAlbaran Lista de productos que vienen en el albarán
     * @return Mensaje de confirmación
     */
    @PostMapping("/{proveedorId}")
    public ResponseEntity<String> guardarAlbaran(
            @PathVariable Integer proveedorId,
            @RequestBody List<ProductoAlbaranEditable> productosAlbaran) {

        albaranService.guardarAlbaran(proveedorId, productosAlbaran);

        return ResponseEntity.ok("✅ Albarán guardado correctamente para el proveedor con ID " + proveedorId);
    }

}
