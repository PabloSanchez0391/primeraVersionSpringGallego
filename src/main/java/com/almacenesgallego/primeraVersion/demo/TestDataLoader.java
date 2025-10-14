package com.almacenesgallego.primeraVersion.demo;

import com.almacenesgallego.primeraVersion.model.Producto;
import com.almacenesgallego.primeraVersion.model.Proveedor;
import com.almacenesgallego.primeraVersion.repository.ProductoRepository;
import com.almacenesgallego.primeraVersion.repository.ProveedorRepository;
import com.almacenesgallego.primeraVersion.service.ProductoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TestDataLoader implements CommandLineRunner {

    private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    private final ProveedorRepository proveedorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("=== Probando conexión a MySQL ===");

//        productoService.limpiarTabla();
        System.out.println("=== Todo limpio ===");

        System.out.println("=== Iniciando carga de datos desde CSV ===");

        // Solo esta línea importa los datos al arrancar la app
//        productoService.importarCSV("src/main/resources/todos-articulos.xlsx - new sheet.csv");

        System.out.println("✅ Importación finalizada");

        // Comprobar que se guarda correctamente
//        productoRepository.findAll().forEach(System.out::println);


        // Guardar proveedor hardcodeado
        Proveedor proveedor = Proveedor.builder()
                .nombre("Tostados de calidad")
//                .referencias(null) // como no tenemos referencias por ahora
                .build();

//        proveedorRepository.deleteAll();
//        entityManager.createNativeQuery("ALTER TABLE proveedor AUTO_INCREMENT = 1").executeUpdate();
//        proveedorRepository.save(proveedor);

        // Comprobar que se guarda correctamente
        proveedorRepository.findAll().forEach(System.out::println);

    }
}
