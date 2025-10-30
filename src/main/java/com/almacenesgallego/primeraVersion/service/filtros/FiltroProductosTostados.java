package com.almacenesgallego.primeraVersion.service.filtros;

import com.almacenesgallego.primeraVersion.model.ProductoAlbaran;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FiltroProductosTostados implements FiltroProductos{

    private static final Pattern FECHA_PATTERN =
            Pattern.compile("Fecha\\s*Caducidad\\s*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern CODIGO_PATTERN = Pattern.compile("(\\d{3,6})");
    private static final Pattern CANTIDAD_PATTERN = Pattern.compile("\\b\\d{1,3},\\d{2}\\b");

    // Lista global de todos los productos (todas las páginas)
//    private static final List<ProductoAlbaran> TODOS_LOS_PRODUCTOS = new ArrayList<>();

    @Override
    public List<ProductoAlbaran> filtrarProductos(String texto) {
//    public String filtrarProductos(String texto) {
        List<String> lines = texto.lines().toList();

        int startIndex = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).toUpperCase().contains("ARTICULO")) {
                startIndex = i + 1;
                break;
            }
        }

        List<ProductoAlbaran> productos = new ArrayList<>();

        String pendingCodigo = null;
        String pendingDescripcion = null;
        String pendingCantidad = null;

        for (int i = startIndex; i < lines.size(); i++) {
            String linea = lines.get(i).trim();
            if (linea.isEmpty()) continue;

            if (linea.toUpperCase().contains("RECIBÍ DE TOTAL CONFORMIDAD") ||
                    linea.toUpperCase().contains("COMPRADOR RENUNCIA")) {
                break;
            }

            Matcher fechaM = FECHA_PATTERN.matcher(linea);
            if (fechaM.find()) {
                String fechaStr = fechaM.group(1);
                if (pendingCodigo != null) {
                    LocalDate fecha = parseFecha(fechaStr);
                    BigDecimal cantidad = parseCantidad(pendingCantidad);
                    // Añadimos lote por defecto "1"
                    ProductoAlbaran p = new ProductoAlbaran(
                            pendingCodigo,
                            pendingDescripcion,
                            cantidad,
                            fecha,
                            "1"
                    );
                    productos.add(p);

                    pendingCodigo = null;
                    pendingDescripcion = null;
                    pendingCantidad = null;
                }
                continue;
            }

            if (linea.toLowerCase().contains("lote") && !FECHA_PATTERN.matcher(linea).find()) {
                continue;
            }

            Matcher codigoM = CODIGO_PATTERN.matcher(linea);
            if (codigoM.find()) {
                String codigo = codigoM.group(1);

                if (linea.toLowerCase().startsWith("lote") || linea.toLowerCase().contains("fecha caducidad")) {
                    continue;
                }

                String afterCode = linea.substring(codigoM.end()).trim();
                String descripcion;

                if (afterCode.contains("|")) {
                    String[] parts = afterCode.split("\\|");
                    String candidate = null;
                    for (String p : parts) {
                        p = p.trim();
                        if (p.isEmpty()) continue;
                        if (p.matches("^\\d+$")) continue;
                        candidate = p;
                        break;
                    }
                    if (candidate == null) candidate = afterCode;
                    descripcion = cleanDescripcion(candidate);
                } else {
                    String candidate = afterCode;
                    int cutIndex = indexOfAnyIgnoreCase(candidate, new String[]{" lote", " fecha", " principal", " etp"});
                    if (cutIndex != -1) candidate = candidate.substring(0, cutIndex);
                    candidate = candidate.replaceAll("\\b\\d{1,3},\\d{2}\\b.*$", "").trim();
                    descripcion = cleanDescripcion(candidate);
                }

                // Buscar cantidad en la línea completa
                Matcher cantidadM = CANTIDAD_PATTERN.matcher(linea);
                String cantidad = cantidadM.find() ? cantidadM.group(0) : "";

                pendingCodigo = codigo;
                pendingDescripcion = descripcion;
                pendingCantidad = cantidad;
            }
        }

        if (pendingCodigo != null) {
            // Añadimos lote por defecto "1"
            productos.add(new ProductoAlbaran(
                    pendingCodigo,
                    pendingDescripcion,
                    parseCantidad(pendingCantidad),
                    null,
                    "1"
            ));
        }

        // Añadir a la lista global
//        TODOS_LOS_PRODUCTOS.addAll(productos);

//        System.out.println("Productos detectados en " + entrada.getName() + ": " + TODOS_LOS_PRODUCTOS.size());
//        System.out.println("Productos detectados en texto OCR: " + TODOS_LOS_PRODUCTOS.size());
        System.out.println("Productos detectados en texto OCR: " + productos.size());

        return productos;
//        return generarTxtProductos(productos);
    }

    private static String cleanDescripcion(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = s.replaceFirst("^[^\\p{L}\\p{N}]+", "");
        s = s.replaceAll("(?i)\\bETP\\b", "");
        s = s.replaceAll("(?i)\\bPRINCIPAL\\b", "");
        s = s.replaceAll("\\b\\d{1,3},\\d{2}\\b.*$", "");
        s = s.replaceAll("\\s{2,}", " ").trim();
        s = s.replaceAll("[|\\p{Punct}]+$", "").trim();
        return s;
    }

    private static int indexOfAnyIgnoreCase(String text, String[] tokens) {
        String lower = text.toLowerCase();
        int best = -1;
        for (String t : tokens) {
            int pos = lower.indexOf(t.toLowerCase());
            if (pos != -1 && (best == -1 || pos < best)) best = pos;
        }
        return best;
    }

    private static LocalDate parseFecha(String fecha) {
        if (fecha == null || fecha.isEmpty()) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(fecha, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("No se pudo parsear la fecha: " + fecha);
            return null;
        }
    }

    private static BigDecimal parseCantidad(String cantidadStr) {
        if (cantidadStr == null || cantidadStr.isEmpty()) return null;
        try {
            // Reemplaza coma por punto para que BigDecimal lo entienda
            String normalized = cantidadStr.replace(",", ".");
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            System.err.println("Cantidad no válida: " + cantidadStr);
            return null;
        }
    }

    /**
     * Genera un String único con todos los productos listados
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

//    public static List<ProductoAlbaran> getTodosLosProductos() {
//        return TODOS_LOS_PRODUCTOS;
//    }
//
//    public static void limpiarProductos() {
//        TODOS_LOS_PRODUCTOS.clear();
//    }
}
