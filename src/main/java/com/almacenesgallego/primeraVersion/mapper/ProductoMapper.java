package com.almacenesgallego.primeraVersion.mapper;

import com.almacenesgallego.primeraVersion.dto.*;
import com.almacenesgallego.primeraVersion.model.*;
import java.util.stream.Collectors;

public class ProductoMapper {

    public static ProductoDTO toDTO(Producto producto) {
        if (producto == null) return null;

        return ProductoDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .precio(producto.getPrecio())
                .referencias(producto.getReferencias() != null ?
                        producto.getReferencias().stream()
                                .map(ProductoMapper::toDTO)
                                .collect(Collectors.toList()) : null)
                .lotes(producto.getLotes() != null ?
                        producto.getLotes().stream()
                                .map(ProductoMapper::toDTO)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    private static ReferenciaProveedorDTO toDTO(ReferenciaProveedor ref) {
        return ReferenciaProveedorDTO.builder()
                .id(ref.getId())
                .codigoProveedor(ref.getCodigoProveedor())
                .proveedorNombre(ref.getProveedor().getNombre())
                .build();
    }

    private static StockLoteDTO toDTO(StockLote lote) {
        return StockLoteDTO.builder()
                .id(lote.getId())
                .cantidad(lote.getCantidad())
                .fechaCaducidad(lote.getFechaCaducidad())
                .numeroLote(lote.getNumeroLote())
                .build();
    }
}
