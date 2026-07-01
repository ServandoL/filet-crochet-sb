package com.filet_crochet.filet_crochet.services;

import com.filet_crochet.filet_crochet.dto.CreatePatternDto;
import com.filet_crochet.filet_crochet.dto.PatternDto;
import com.filet_crochet.filet_crochet.dto.UpdatePatternDto;
import com.filet_crochet.filet_crochet.repositories.PatternsRepositoryImpl;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PatternsService {
    private static final Logger log = LoggerFactory.getLogger(PatternsService.class);
    @Autowired
    private final PatternsRepositoryImpl patternRepository;

    public PatternsService(PatternsRepositoryImpl patternRepository) {
        this.patternRepository = patternRepository;
    }

    public DeleteResult deletePattern(String id) {
        try {
            return patternRepository.deleteById(new ObjectId(id));
        } catch (Exception error) {
            log.warn("Error deleting pattern: {}", id, error);
            return null;
        }
    }

    public List<PatternDto> getPatterns() {
        return patternRepository.findAll();
    }

    public Optional<PatternDto> patchPattern(String id, UpdatePatternDto pattern) {
        try {
            var result = patternRepository.updatePattern(new ObjectId(id), pattern);
            return Optional.ofNullable(result);
        } catch (Exception error) {
            log.warn("Error patching pattern: {}", id, error);
            return Optional.empty();
        }
    }

    public Optional<PatternDto> getById(String id) {
        try {
            return Optional.ofNullable(patternRepository.findById(new ObjectId(id)));
        } catch (Exception error) {
            log.warn("Error getting pattern: {}", id, error);
            return Optional.empty();
        }
    }

    public PatternDto createPattern(@NonNull CreatePatternDto pattern) {
        var now = LocalDateTime.now();
        var toInsert = new PatternDto(
                new ObjectId(),
                pattern.name(),
                pattern.rows(),
                pattern.cols(),
                pattern.cells(),
                null,
                null,
                now,
                now
        );
        return patternRepository.createPattern(toInsert);
    }

}
