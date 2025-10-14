package com.almacenesgallego.primeraVersion.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLoteDTO {
    private Integer id;
    private Integer cantidad;
    private LocalDate fechaCaducidad;
    private String numeroLote;
}
