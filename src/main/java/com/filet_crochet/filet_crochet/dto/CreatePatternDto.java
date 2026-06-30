package com.filet_crochet.filet_crochet.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"row", "col"})
public record CreatePatternDto(
        @NotBlank(message = "Pattern name cannot be blank")
        String name,
        @NotNull(message = "Pattern rows cannot be null")
        Integer rows,
        @NotNull(message = "Pattern cols cannot be null")
        Integer cols,
        @NotNull(message = "Pattern cells cannot be null")
        List<FilledCellDto> cells,
        String updatedAt,
        String createdAt
) {}