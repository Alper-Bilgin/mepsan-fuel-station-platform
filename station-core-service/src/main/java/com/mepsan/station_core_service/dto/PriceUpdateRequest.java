package com.mepsan.station_core_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PriceUpdateRequest(
        @NotNull @Positive BigDecimal newPrice
) {}
