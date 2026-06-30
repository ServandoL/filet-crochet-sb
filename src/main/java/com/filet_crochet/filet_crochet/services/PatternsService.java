package com.filet_crochet.filet_crochet.services;

import com.filet_crochet.filet_crochet.dto.CreatePatternDto;
import com.filet_crochet.filet_crochet.dto.FilledCellDto;
import com.filet_crochet.filet_crochet.dto.PatternDto;
import com.filet_crochet.filet_crochet.dto.UpdatePatternDto;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatternsService {
    private static final Logger log = LoggerFactory.getLogger(PatternsService.class);
    private final MongoTemplate mongoTemplate;
    private final ProgressService progressService;
    public static final String PATTERNS_COLLECTION = "PATTERNS";

    public PatternsService(MongoTemplate mongoTemplate, ProgressService progressService) {
        this.mongoTemplate = mongoTemplate;
        this.progressService = progressService;
    }

    public DeleteResult deletePattern(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId));
            DeleteResult deleteResult = mongoTemplate.remove(query, PatternDto.class);

            if (deleteResult.wasAcknowledged() && deleteResult.getDeletedCount() > 0) {
                // Cascade-delete the associated progress document (ignore errors — orphan is acceptable)
                try {
                    progressService.deleteById(id);
                } catch (Exception e) {
                    log.warn("Failed to cascade delete progress for pattern: {}", id, e);
                }
            }
            return deleteResult;
        } catch (Exception error) {
            log.warn("Error deleting pattern: {}", id, error);
            return null;
        }
    }

    public List<PatternDto> getPatterns() {
        return mongoTemplate.find(new Query(), PatternDto.class, PATTERNS_COLLECTION);
    }

    public Optional<PatternDto> patchPattern(String id, UpdatePatternDto pattern) {
        try {
            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId));

            Update update = new Update();
            update.set("updatedAt", LocalDateTime.now());

            if (pattern.name() != null) {
                update.set("name", pattern.name());
            }

            if (pattern.cells() != null) {
                List<FilledCellDto> cells = pattern.cells().stream()
                        .map((FilledCellDto c) -> new FilledCellDto(c.row(), c.col()))
                        .collect(Collectors.toList());
                update.set("cells", cells);
            }

            if (pattern.sectionRowInterval() != null) {
                update.set("sectionRowInterval", pattern.sectionRowInterval());
            }

            if (pattern.sectionColInterval() != null) {
                update.set("sectionColInterval", pattern.sectionColInterval());
            }

            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, PatternDto.class);
            if (updateResult.wasAcknowledged() && updateResult.getModifiedCount() > 0) {
                return Optional.ofNullable(mongoTemplate.findOne(query, PatternDto.class, PATTERNS_COLLECTION));
            }
            return Optional.empty();
        } catch (Exception error) {
            log.warn("Error patching pattern: {}", id, error);
            return Optional.empty();
        }
    }

    public Optional<PatternDto> getById(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId));
            return Optional.ofNullable(mongoTemplate.findOne(query, PatternDto.class, PATTERNS_COLLECTION));
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
        return mongoTemplate.insert(toInsert, PATTERNS_COLLECTION);
    }

}
