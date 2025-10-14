package com.almacenesgallego.primeraVersion.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {
    private String id;
    private String nombre;
    private BigDecimal precio;

    // Incluimos detalles si queremos
    private List<ReferenciaProveedorDTO> referencias;
    private List<StockLoteDTO> lotes;
}
