package com.filet_crochet.filet_crochet.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;

public record UpsertProgressDto(
        @NotNull(message = "Highlighted cells cannot be null")
        ArrayList<ArrayList<Integer>> highlightedCells,
        @NotNull(message = "UpdatedAt cannot be null")
        LocalDateTime updatedAt) {
}
