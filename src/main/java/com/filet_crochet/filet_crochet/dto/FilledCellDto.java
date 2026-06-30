package com.filet_crochet.filet_crochet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"row", "col"})
public record FilledCellDto(
        @NotNull(message = "Row cannot be null")
        Integer row,
        @NotNull(message = "Col cannot be null")
        Integer col) {
}