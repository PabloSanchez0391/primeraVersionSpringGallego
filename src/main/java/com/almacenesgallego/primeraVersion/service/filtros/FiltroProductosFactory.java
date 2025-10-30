package com.almacenesgallego.primeraVersion.service.filtros;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FiltroProductosFactory {

    private final Map<Long, FiltroProductos> filtros = new HashMap<>();

    public FiltroProductosFactory() {
        filtros.put(1L, new FiltroProductosTostados());
        filtros.put(2L, new FiltroProductosPeñaSanta());
        // añade más según los proveedores disponibles
    }

    public FiltroProductos getFiltro(Long proveedorId) {
        return filtros.getOrDefault(proveedorId, new FiltroGenerico());
    }
}
