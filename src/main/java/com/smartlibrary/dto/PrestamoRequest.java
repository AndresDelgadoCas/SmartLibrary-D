package com.smartlibrary.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PrestamoRequest(
        @NotNull @Positive Long estudianteId,
        @NotNull @Positive Long libroId
) {
}
