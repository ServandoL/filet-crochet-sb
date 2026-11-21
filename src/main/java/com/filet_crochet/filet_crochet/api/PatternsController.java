package com.filet_crochet.filet_crochet.api;

import com.home.household.CurrentMember;
import com.home.household.HouseholdEvent.Action;
import com.home.household.HouseholdEvents;
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

    private final HouseholdEvents events;

    public PatternsController(PatternsService patternsService, ProgressService progressService, HouseholdEvents events) {
        this.patternsService = patternsService;
        this.progressService = progressService;
        this.events = events;
    }

    @GetMapping
    public ResponseEntity<List<PatternDto>> getAllPatterns(CurrentMember member) {
        return ResponseEntity.ok(patternsService.getPatterns(member.householdId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericMessageResponseDto> deletePattern(CurrentMember member, @PathVariable @ValidObjectId String id) {
        // Logic to delete the pattern goes here
        var deleteResult = patternsService.deletePattern(member.householdId(), id);
        if (deleteResult == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericMessageResponseDto("Error deleting pattern"));
        }
        if (deleteResult.getDeletedCount() == 0) {
            return ResponseEntity.notFound().build();
        }
        events.publish(member, "patterns", Action.DELETED, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PatternDto> updatePattern(CurrentMember member, @PathVariable @ValidObjectId String id, @Valid @RequestBody UpdatePatternDto patternDto) {
        return patternsService.patchPattern(member.householdId(), id, patternDto)
                .map((pattern) -> {
                    events.publish(member, "patterns", Action.UPDATED, id);
                    return ResponseEntity.ok(pattern);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatternDto> getPatternById(
            CurrentMember member, @PathVariable @ValidObjectId String id) {
        return patternsService.getById(member.householdId(), id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PatternDto> createPattern(CurrentMember member, @Valid @RequestBody CreatePatternDto patternDto) {
        var result = patternsService.createPattern(member.householdId(), patternDto);
        events.publish(member, "patterns", Action.CREATED, result._id().toHexString());
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(result._id()).toUri();
        return ResponseEntity.created(location).body(result);
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<ProgressDto> getProgressByPatternId(CurrentMember member, @PathVariable @ValidObjectId String id) {
        return progressService.getById(member.householdId(), id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<ProgressDto> updateProgressByPatternId(CurrentMember member, @PathVariable @ValidObjectId String id, @Valid @RequestBody UpsertProgressDto progressDto) {
        // A pattern has one progress document, so the event names the pattern.
        return progressService.upsertById(member.householdId(), id, progressDto)
                .map((progress) -> {
                    events.publish(member, "progress", Action.UPDATED, id);
                    return ResponseEntity.ok(progress);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/progress")
    public ResponseEntity<GenericMessageResponseDto> deleteProgressByPatternId(CurrentMember member, @PathVariable @ValidObjectId String id) {
        progressService.deleteById(member.householdId(), id);
        events.publish(member, "progress", Action.DELETED, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
