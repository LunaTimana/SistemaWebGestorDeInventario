package com.emsstore.backend.dto;

import java.time.LocalDateTime;

import com.emsstore.backend.model.MovimientoInventario;
import com.emsstore.backend.model.TipoMovimiento;

public record HistorialItemResponse(
        Integer id,
        String tipo,
        String motivo,
        int cantidad,
        LocalDateTime fecha,
        String documento) {

    public static HistorialItemResponse desde(MovimientoInventario m) {
        // Con signo: las entradas suman (+) y las salidas restan (-)
        int cantidadConSigno = m.getTipo() == TipoMovimiento.entrada ? m.getCantidad() : -m.getCantidad();
        return new HistorialItemResponse(
                m.getId(),
                m.getTipo().name(),
                m.getMotivo().name(),
                cantidadConSigno,
                m.getFecha(),
                m.getDocumento());
    }
}