package com.almacenesgallego.primeraVersion.service.filtros;

import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;
import java.util.List;

public interface FiltroProductos {
    List<ProductoAlbaran> filtrarProductos(String texto);
//    String filtrarProductos(String texto);
}
