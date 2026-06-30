package com.filet_crochet.filet_crochet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"row", "col"})
public record ProgressDto(
        @NotNull(message = "updatedAt cannot be null")
        LocalDateTime updatedAt,
        @NotNull(message = "Cells cannot be null")
        List<FilledCellDto> cells) {
}
