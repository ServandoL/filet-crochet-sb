package com.filet_crochet.filet_crochet.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;

public record CreatePatternDto(
        @NotBlank(message = "Pattern name cannot be blank")
        String name,
        @NotNull(message = "Pattern rows cannot be null")
        Integer rows,
        @NotNull(message = "Pattern cols cannot be null")
        Integer cols,
        @NotNull(message = "Pattern cells cannot be null")
        ArrayList<ArrayList<Integer>> cells,
        String updatedAt,
        String createdAt
) {}