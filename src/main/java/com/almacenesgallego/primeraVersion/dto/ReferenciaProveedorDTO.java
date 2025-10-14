package com.almacenesgallego.primeraVersion.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenciaProveedorDTO {
    private Integer id;
    private String codigoProveedor;
    private String proveedorNombre;
}
