package com.almacenesgallego.primeraVersion.service;

import com.almacenesgallego.primeraVersion.model.*;
import com.almacenesgallego.primeraVersion.repository.*;
import com.almacenesgallego.primeraVersion.wrapper.ProductoAlbaranEditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbaranService {

    private final ProveedorRepository proveedorRepository;
    private final ReferenciaProveedorRepository referenciaProveedorRepository;
    private final StockLoteRepository stockLoteRepository;

    @Transactional
    public void guardarAlbaran(Integer proveedorId, List<ProductoAlbaranEditable> productosAlbaran) {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new RuntimeException("Proveedor con id=" + proveedorId + " no encontrado"));

        for (ProductoAlbaranEditable pa : productosAlbaran) {
            ReferenciaProveedor ref = referenciaProveedorRepository
                    .findByProveedorAndCodigoProveedor(proveedor, pa.getCodigo())
                    .orElseThrow(() -> new RuntimeException("No se encontró referencia para código proveedor: " + pa.getCodigo()));

            Producto productoInterno = ref.getProducto();

            StockLote lote = StockLote.builder()
                    .producto(productoInterno)
                    .cantidad(pa.getCantidad().intValue())
                    .fechaCaducidad(pa.getFechaCaducidad())
                    .numeroLote(pa.getNumeroLote())
                    .build();

            stockLoteRepository.save(lote);
        }
    }

}
