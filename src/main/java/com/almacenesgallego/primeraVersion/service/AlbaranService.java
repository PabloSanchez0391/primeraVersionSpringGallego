package com.almacenesgallego.primeraVersion.service;

import com.almacenesgallego.primeraVersion.model.*;
import com.almacenesgallego.primeraVersion.repository.*;
import com.almacenesgallego.primeraVersion.wrapper.ProductoAlbaranEditable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AlbaranService {

    private final ProveedorRepository proveedorRepository;
    private final ReferenciaProveedorRepository referenciaProveedorRepository;
    private final StockLoteRepository stockLoteRepository;

    public AlbaranService(ProveedorRepository proveedorRepository,
                          ReferenciaProveedorRepository referenciaProveedorRepository,
                          StockLoteRepository stockLoteRepository) {
        this.proveedorRepository = proveedorRepository;
        this.referenciaProveedorRepository = referenciaProveedorRepository;
        this.stockLoteRepository = stockLoteRepository;
    }

    @Transactional
    public void guardarAlbaran(List<ProductoAlbaranEditable> productosAlbaran) {
        Proveedor proveedor = proveedorRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Proveedor con id=1 no encontrado"));

        for (ProductoAlbaranEditable pa : productosAlbaran) {
            // Buscar la referencia del proveedor
            ReferenciaProveedor ref = referenciaProveedorRepository
                    .findByProveedorAndCodigoProveedor(proveedor, pa.getCodigo())
                    .orElseThrow(() -> new RuntimeException("No se encontró referencia para código proveedor: " + pa.getCodigo()));

            Producto productoInterno = ref.getProducto();

            // Crear nuevo lote
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
