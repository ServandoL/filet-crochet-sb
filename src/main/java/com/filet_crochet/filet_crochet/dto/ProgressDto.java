package com.filet_crochet.filet_crochet.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;

public record ProgressDto(
        @NotNull(message = "updatedAt cannot be null")
        LocalDateTime updatedAt,
        @NotNull(message = "Cells cannot be null")
        ArrayList<ArrayList<Integer>> highlightedCells) {
}
