package com.almacenesgallego.primeraVersion.wrapper;

import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoAlbaranEditable {
    private String codigo;
    private String descripcion;
    private BigDecimal cantidad;
    private LocalDate fechaCaducidad;
    private String numeroLote;

    public ProductoAlbaranEditable(ProductoAlbaran pa) {
        this.codigo = pa.codigo();
        this.descripcion = pa.descripcion();
        this.cantidad = pa.cantidad();
        this.fechaCaducidad = pa.fechaCaducidad();
        this.numeroLote = pa.numeroLote();
    }
}
