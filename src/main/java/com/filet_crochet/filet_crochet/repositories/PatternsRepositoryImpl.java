package com.filet_crochet.filet_crochet.repositories;

import com.filet_crochet.filet_crochet.dto.FilledCellDto;
import com.filet_crochet.filet_crochet.dto.PatternDto;
import com.filet_crochet.filet_crochet.dto.UpdatePatternDto;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PatternsRepositoryImpl implements PatternsRepository {
    public static final String PATTERNS_COLLECTION = "PATTERNS";
    private static final Logger log = LoggerFactory.getLogger(PatternsRepositoryImpl.class);
    private final MongoTemplate mongoTemplate;
    private final ProgressRepositoryImpl progressRepository;

    public PatternsRepositoryImpl(MongoTemplate mongoTemplate, ProgressRepositoryImpl progressRepository) {
        this.mongoTemplate = mongoTemplate;
        this.progressRepository = progressRepository;
    }

    @Override
    public PatternDto findById(ObjectId id) {
        return mongoTemplate.findById(id, PatternDto.class, PATTERNS_COLLECTION);
    }

    @Override
    public DeleteResult deleteById(ObjectId id) {
        var query = new Query(Criteria.where("_id").is(id));
        var deleteResult = mongoTemplate.remove(query, PATTERNS_COLLECTION);
        if (deleteResult.wasAcknowledged() && deleteResult.getDeletedCount() > 0) {
            // Cascade-delete the associated progress document (ignore errors — orphan is acceptable)
            try {
                progressRepository.deleteById(id);
            } catch (Exception e) {
                log.warn("Failed to cascade delete progress for pattern: {}", id, e);
            }
        }
        return deleteResult;
    }

    @Override
    public List<PatternDto> findAll() {
        return mongoTemplate.findAll(PatternDto.class, PATTERNS_COLLECTION);
    }

    @Override
    public PatternDto updatePattern(ObjectId id, UpdatePatternDto pattern) {
        Query query = new Query(Criteria.where("_id").is(id));

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
            return mongoTemplate.findOne(query, PatternDto.class, PATTERNS_COLLECTION);
        }
        return null;
    }

    @Override
    public PatternDto createPattern(PatternDto pattern) {
        return mongoTemplate.insert(pattern, PATTERNS_COLLECTION);
    }
}
