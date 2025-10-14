package com.almacenesgallego.primeraVersion.repository;

import com.almacenesgallego.primeraVersion.model.Proveedor;
import com.almacenesgallego.primeraVersion.model.ReferenciaProveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferenciaProveedorRepository extends JpaRepository<ReferenciaProveedor, Integer> {
    Optional<ReferenciaProveedor> findByProveedorAndCodigoProveedor(Proveedor proveedor, String codigoProveedor);
}