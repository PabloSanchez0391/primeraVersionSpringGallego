package com.almacenesgallego.primeraVersion.repository;


import com.almacenesgallego.primeraVersion.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {
    // Ejemplo de método personalizado:
    Producto findByNombre(String nombre);
}
