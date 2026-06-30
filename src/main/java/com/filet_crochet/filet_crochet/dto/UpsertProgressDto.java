package com.filet_crochet.filet_crochet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"row", "col"})
public record UpsertProgressDto(
        @NotNull(message = "Highlighted cells cannot be null")
        List<FilledCellDto> highlightedCells) {
}
