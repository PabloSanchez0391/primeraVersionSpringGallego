package com.almacenesgallego.primeraVersion.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoAlbaran(
        String codigo,
        String descripcion,
        BigDecimal cantidad,
        LocalDate fechaCaducidad,
        String numeroLote
) {}

