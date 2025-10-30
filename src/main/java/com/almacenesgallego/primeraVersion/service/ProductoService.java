package com.almacenesgallego.primeraVersion.service;

import com.almacenesgallego.primeraVersion.dto.ProductoDTO;
import com.almacenesgallego.primeraVersion.mapper.ProductoMapper;
import com.almacenesgallego.primeraVersion.model.Producto;
import com.almacenesgallego.primeraVersion.repository.ProductoRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Optional<Producto> findById(String id) {
        return productoRepository.findById(id);
    }

    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    public void delete(String id) {
        productoRepository.deleteById(id);
    }

    public List<ProductoDTO> obtenerTodos() {
        return productoRepository.findAll()
                .stream()
                .map(ProductoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void limpiarTabla() {
        productoRepository.deleteAll();
        System.out.println("🧹 Tabla 'producto' vaciada correctamente.");
    }

    /**
     * Importa productos desde un archivo CSV al repositorio de base de datos.
     * El CSV debe tener el formato: id, nombre, precio
     */
    @Transactional
    public void importarCSV(String csvFilePath) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            String[] fila;
            List<Producto> productos = new ArrayList<>();

            while ((fila = reader.readNext()) != null) {
                if (fila.length < 3) continue; // Evita líneas incompletas

                try {
                    BigDecimal precio = new BigDecimal(fila[2].replace(",", ".").replace("\"", "").trim());

                    Producto producto = Producto.builder()
                            .id(fila[0].trim())
                            .nombre(fila[1].trim())
                            .precio(precio)
                            .build();

                    productos.add(producto);

                } catch (NumberFormatException e) {
//                    log.warn("⚠️ Precio inválido en línea: {}", (Object) fila);
                    System.out.println("⚠️ Precio inválido en línea: {}");
                }
            }

            productoRepository.saveAll(productos);
            System.out.println("✅ Importación completada desde: " + csvFilePath);

        } catch (Exception e) {
            System.err.println("❌ Error al importar CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void importarCSV(InputStream inputStream) {
    }

}
