package com.filet_crochet.filet_crochet.dto;
import java.util.ArrayList;

public record UpdatePatternDto(
        String name,
        ArrayList<ArrayList<Integer>> cells,
        Integer sectionRowInterval,
        Integer sectionColInterval) {
}
