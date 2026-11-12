package com.smartlibrary.dto;

import java.time.LocalDate;

public record PrestamoResponse(
        Long id,
        Long estudianteId,
        String nombreEstudiante,
        Long libroId,
        String tituloLibro,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucion
) {
}
