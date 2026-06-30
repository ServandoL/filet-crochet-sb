package com.filet_crochet.filet_crochet.api;

import com.filet_crochet.filet_crochet.dto.*;
import com.filet_crochet.filet_crochet.services.PatternsService;
import com.filet_crochet.filet_crochet.services.ProgressService;
import com.filet_crochet.filet_crochet.validators.PatternsValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/patterns")
public class PatternsController {
    private final PatternsValidator patternsValidator;
    private final PatternsService patternsService;
    private final ProgressService progressService;

    public PatternsController(PatternsValidator patternsValidator, PatternsService patternsService, ProgressService progressService) {
        this.patternsValidator = patternsValidator;
        this.patternsService = patternsService;
        this.progressService = progressService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericMessageResponseDto> deletePattern(@PathVariable String id) {
        if (!patternsValidator.validateId(id)) {
            return ResponseEntity.badRequest().body(new GenericMessageResponseDto("Invalid pattern ID"));
        }
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

    @GetMapping
    public ResponseEntity<List<PatternDto>> getAllPatterns() {
        return ResponseEntity.ok(patternsService.getPatterns());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PatternDto> updatePattern(@PathVariable String id, @Valid @RequestBody UpdatePatternDto patternDto) {
        if (!patternsValidator.validateId(id)) {
            return ResponseEntity.badRequest().build();
        }
        return patternsService.patchPattern(id, patternDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatternDto> getPatternById(
            @PathVariable() String id) {
        if (!patternsValidator.validateId(id)) {
            return ResponseEntity.badRequest().build();
        }
        return patternsService.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PatternDto> createPattern(@Valid @RequestBody CreatePatternDto patternDto) {
        var result = patternsService.createPattern(patternDto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(result._id()).toUri();
        return ResponseEntity.created(location).body(result);
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<ProgressDto> getProgressByPatternId(@PathVariable() String id) {
        if (!patternsValidator.validateId(id)) {
            return ResponseEntity.badRequest().build();
        }
        return progressService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<ProgressDto> updateProgressByPatternId(@PathVariable() String id, @Valid @RequestBody UpsertProgressDto progressDto) {
        if (!patternsValidator.validateId(id)) {
            return ResponseEntity.badRequest().build();
        }
        return progressService.upsertById(id, progressDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/progress")
    public ResponseEntity<GenericMessageResponseDto> deleteProgressByPatternId(@PathVariable() String id) {
        if (!patternsValidator.validateId(id)) {
            return ResponseEntity.badRequest().body(new GenericMessageResponseDto("Invalid pattern ID"));
        }
        progressService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
