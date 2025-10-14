package com.almacenesgallego.primeraVersion.controller;

import com.almacenesgallego.primeraVersion.dto.ProductoDTO;
import com.almacenesgallego.primeraVersion.mapper.ProductoMapper;
import com.almacenesgallego.primeraVersion.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoRepository productoRepository;

    @GetMapping
    public List<ProductoDTO> getAllProductos() {
        return productoRepository.findAll().stream()
                .map(ProductoMapper::toDTO)
                .collect(Collectors.toList());
    }
}
