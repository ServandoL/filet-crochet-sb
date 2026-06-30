package com.filet_crochet.filet_crochet.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"row", "col"})
public record UpdatePatternDto(
        String name,
        List<FilledCellDto> cells,
        Integer sectionRowInterval,
        Integer sectionColInterval) {
}
