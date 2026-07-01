package com.filet_crochet.filet_crochet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;

public record PatternDto(
        @Id
        @JsonSerialize(using = ToStringSerializer.class)
        ObjectId _id,
        @NotBlank(message = "Pattern name cannot be blank")
        String name,
        @NotNull(message = "Pattern rows cannot be null")
        Integer rows,
        @NotNull(message = "Pattern cols cannot be null")
        Integer cols,
        @NotNull(message = "Pattern cells cannot be null")
        ArrayList<ArrayList<Integer>> cells,
        @NotNull(message = "Section row interval cannot be null")
        Integer sectionRowInterval,
        @NotNull(message = "Section col interval cannot be null")
        Integer sectionColInterval,
        LocalDateTime updatedAt,
        LocalDateTime createdAt
) {
}