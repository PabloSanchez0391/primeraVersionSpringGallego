package com.almacenesgallego.primeraVersion.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "referencias")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    private List<ReferenciaProveedor> referencias;
}


