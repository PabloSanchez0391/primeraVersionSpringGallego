package com.almacenesgallego.primeraVersion.service;

import com.almacenesgallego.primeraVersion.model.Producto;
import com.almacenesgallego.primeraVersion.repository.ProductoRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

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

                Producto producto = Producto.builder()
                        .id(fila[0].trim())
                        .nombre(fila[1].trim())
                        .precio(new BigDecimal(fila[2].replace(",", ".").replace("\"", "").trim()))
                        .build();

                productos.add(producto);
            }

            productoRepository.saveAll(productos);
            System.out.println("✅ Importación completada desde: " + csvFilePath);

        } catch (Exception e) {
            System.err.println("❌ Error al importar CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
