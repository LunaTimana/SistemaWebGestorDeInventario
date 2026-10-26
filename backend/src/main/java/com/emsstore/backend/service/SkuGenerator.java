package com.emsstore.backend.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Regla del SKU: MARCA-MODELO-TALLA-COLOR
 *  - MARCA : una palabra -> 3 primeras letras (Nike = NIK); varias -> iniciales (Under Armour = UA)
 *  - MODELO: iniciales de cada palabra, sin "de", "la", etc.; los números se conservan enteros
 *  - TALLA : sin espacios ni símbolos (US 10 = US10)
 *  - COLOR : 3 primeras letras de cada palabra (Azul/Rojo = AZUROJ)
 */
public final class SkuGenerator {

    private static final Set<String> PALABRAS_MENORES = Set.of("DE", "DEL", "LA", "EL", "Y", "CON", "PARA");

    private SkuGenerator() {
    }

    public static String generar(String marca, String modelo, String talla, String color) {
        return String.join("-", codigoMarca(marca), codigoModelo(modelo), unir(talla), codigoColor(color));
    }

    private static String codigoMarca(String marca) {
        List<String> palabras = palabras(marca);
        if (palabras.size() == 1) {
            String palabra = palabras.get(0);
            return palabra.substring(0, Math.min(3, palabra.length()));
        }
        StringBuilder codigo = new StringBuilder();
        for (String palabra : palabras) {
            codigo.append(palabra.charAt(0));
        }
        return codigo.toString();
    }

    private static String codigoModelo(String modelo) {
        StringBuilder codigo = new StringBuilder();
        for (String palabra : palabras(modelo)) {
            if (PALABRAS_MENORES.contains(palabra)) {
                continue;
            }
            if (Character.isDigit(palabra.charAt(0))) {
                codigo.append(palabra);
            } else {
                codigo.append(palabra.charAt(0));
            }
        }
        return codigo.toString();
    }

    private static String codigoColor(String color) {
        StringBuilder codigo = new StringBuilder();
        for (String palabra : palabras(color)) {
            codigo.append(palabra, 0, Math.min(3, palabra.length()));
        }
        return codigo.toString();
    }

    private static String unir(String texto) {
        return String.join("", palabras(texto));
    }

    // Mayúsculas, sin tildes ni símbolos, separado en palabras
    private static List<String> palabras(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        List<String> resultado = new ArrayList<>();
        for (String palabra : sinTildes.toUpperCase().split("[^A-Z0-9]+")) {
            if (!palabra.isEmpty()) {
                resultado.add(palabra);
            }
        }
        return resultado;
    }
}