package com.filet_crochet.filet_crochet.api;

import com.filet_crochet.filet_crochet.dto.*;
import com.filet_crochet.filet_crochet.services.PatternsService;
import com.filet_crochet.filet_crochet.services.ProgressService;
import com.filet_crochet.filet_crochet.validators.ValidObjectId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/patterns")
@Validated
public class PatternsController {
    private final PatternsService patternsService;
    private final ProgressService progressService;

    public PatternsController(PatternsService patternsService, ProgressService progressService) {
        this.patternsService = patternsService;
        this.progressService = progressService;
    }

    @GetMapping
    public ResponseEntity<List<PatternDto>> getAllPatterns() {
        return ResponseEntity.ok(patternsService.getPatterns());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericMessageResponseDto> deletePattern(@PathVariable @ValidObjectId String id) {
        // Logic to delete the pattern goes here
        var deleteResult = patternsService.deletePattern(id);
        if (deleteResult == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericMessageResponseDto("Error deleting pattern"));
        }
        if (deleteResult.getDeletedCount() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PatternDto> updatePattern(@PathVariable @ValidObjectId String id, @Valid @RequestBody UpdatePatternDto patternDto) {
        return patternsService.patchPattern(id, patternDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatternDto> getPatternById(
            @PathVariable @ValidObjectId String id) {
        return patternsService.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PatternDto> createPattern(@Valid @RequestBody CreatePatternDto patternDto) {
        var result = patternsService.createPattern(patternDto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(result._id()).toUri();
        return ResponseEntity.created(location).body(result);
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<ProgressDto> getProgressByPatternId(@PathVariable @ValidObjectId String id) {
        return progressService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<ProgressDto> updateProgressByPatternId(@PathVariable @ValidObjectId String id, @Valid @RequestBody UpsertProgressDto progressDto) {
        return progressService.upsertById(id, progressDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/progress")
    public ResponseEntity<GenericMessageResponseDto> deleteProgressByPatternId(@PathVariable @ValidObjectId String id) {
        progressService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
