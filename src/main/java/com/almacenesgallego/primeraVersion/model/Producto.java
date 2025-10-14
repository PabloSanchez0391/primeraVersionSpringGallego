package com.almacenesgallego.primeraVersion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"referencias", "lotes"}) // Evita LazyInitializationException
public class Producto {

    @Id
    private String id; // Código interno

    private String nombre;

    private BigDecimal precio;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ReferenciaProveedor> referencias;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<StockLote> lotes;
}
