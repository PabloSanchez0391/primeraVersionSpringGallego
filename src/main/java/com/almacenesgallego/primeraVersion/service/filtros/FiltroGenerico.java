package com.almacenesgallego.primeraVersion.service.filtros;

import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;

import java.util.Collections;
import java.util.List;

public class FiltroGenerico implements FiltroProductos {
//    @Override
//    public List<ProductoAlbaran> filtrarProductos(String texto) {
//        return List.of();
//    }

//    @Override
//    public String filtrarProductos(String texto) {
//        return "No se encontró un filtro específico para este proveedor.";
//    }

    @Override
    public List<ProductoAlbaran> filtrarProductos(String texto) {
        System.out.println("No se encontró un filtro específico para este proveedor.");
        return Collections.emptyList();
    }

}
