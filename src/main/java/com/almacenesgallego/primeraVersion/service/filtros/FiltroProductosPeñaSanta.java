package com.almacenesgallego.primeraVersion.service.filtros;


import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FiltroProductosPeñaSanta implements FiltroProductos {

    private static final Pattern PRODUCTO_PATTERN = Pattern.compile(
            "(?<codigo>\\d{5})\\s+(?<ean>\\d{13})\\s+(?<descripcion>.+?)\\s+\\*?\\d*\\s+\\d+\\s+\\d+\\s+(?<cantidad>[\\d,.]+)\\s+[A-Z]{2}",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LOTE_PATTERN = Pattern.compile(
            "Lote\\s+(?<lote>\\w+)\\s+Consumo\\s+preferente\\s+(?<fecha>\\d{2}\\.\\d{2}\\.\\d{4})",
            Pattern.CASE_INSENSITIVE
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // Lista global de todos los productos parseados
//    private static final List<ProductoAlbaran> TODOS_LOS_PRODUCTOS = new ArrayList<>();

    /**
     * Parsea el texto OCR, genera objetos ProductoAlbaran y los almacena en la lista estática.
     * Devuelve un String con la información de los productos encontrados.
     */
    @Override
    public List<ProductoAlbaran> filtrarProductos(String texto) {
//    public String filtrarProductos(String texto) {
        List<ProductoAlbaran> productos = new ArrayList<>();

//        TODOS_LOS_PRODUCTOS.clear(); // Limpiar lista anterior

        Matcher productoMatcher = PRODUCTO_PATTERN.matcher(texto);
        Matcher loteMatcher = LOTE_PATTERN.matcher(texto);

        List<Map<String, String>> bloques = new ArrayList<>();

        // Extraer productos
        while (productoMatcher.find()) {
            Map<String, String> datos = new HashMap<>();
            datos.put("codigo", productoMatcher.group("codigo"));
            datos.put("descripcion", productoMatcher.group("descripcion").trim());
            datos.put("cantidad", productoMatcher.group("cantidad").replace(",", "."));
            bloques.add(datos);
        }

        // Extraer lotes y fechas
        int index = 0;
        while (loteMatcher.find() && index < bloques.size()) {
            Map<String, String> datos = bloques.get(index);
            datos.put("lote", loteMatcher.group("lote"));
            datos.put("fecha", loteMatcher.group("fecha"));
            index++;
        }

        // Construir objetos ProductoAlbaran
        for (Map<String, String> datos : bloques) {
            try {
                String codigo = datos.get("codigo");
                String descripcion = datos.get("descripcion");
                BigDecimal cantidad = new BigDecimal(datos.getOrDefault("cantidad", "0"));
                LocalDate fecha = datos.containsKey("fecha")
                        ? LocalDate.parse(datos.get("fecha"), DATE_FORMATTER)
                        : null;
                String lote = datos.getOrDefault("lote", "");

//                TODOS_LOS_PRODUCTOS.add(new ProductoAlbaran(codigo, descripcion, cantidad, fecha, lote));
                productos.add(new ProductoAlbaran(codigo, descripcion, cantidad, fecha, lote));

            } catch (Exception e) {
                System.err.println("Error al parsear producto: " + e.getMessage());
            }
        }
        return productos;
//        return generarTxtProductos(TODOS_LOS_PRODUCTOS);
    }

    /**
     * Devuelve la lista completa de productos parseados.
     */
//    public static List<ProductoAlbaran> getTodosLosProductos() {
//        return Collections.unmodifiableList(TODOS_LOS_PRODUCTOS);
//    }

    /**
     * Genera un String único con todos los productos listados.
     */
    private static String generarTxtProductos(List<ProductoAlbaran> productos) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < productos.size(); i++) {
            ProductoAlbaran p = productos.get(i);

            out.append(p.codigo() == null ? "" : p.codigo())
                    .append(" | ")
                    .append(p.descripcion() == null ? "" : p.descripcion());

            if (p.cantidad() != null) {
                out.append(" ").append(formatCantidad(p.cantidad()));
            }

            if (p.fechaCaducidad() != null) {
                out.append(" Fecha Caducidad ").append(p.fechaCaducidad().format(df));
            }

            if (p.numeroLote() != null && !p.numeroLote().isBlank()) {
                out.append(" Lote ").append(p.numeroLote());
            }

            if (i < productos.size() - 1) out.append("\n");
        }

        return out.toString();
    }

    /**
     * Formatea correctamente las cantidades (quita ceros innecesarios o notación científica).
     */
    private static String formatCantidad(BigDecimal cantidad) {
        try {
            BigDecimal cleaned = cantidad.stripTrailingZeros();
            int scale = cleaned.scale();
            if (scale <= 0) {
                return cleaned.toBigInteger().toString();
            } else {
                return cleaned.toPlainString();
            }
        } catch (ArithmeticException ex) {
            return cantidad.toPlainString();
        }
    }
//    public static void limpiarProductos() {
//        TODOS_LOS_PRODUCTOS.clear();
//    }

}
