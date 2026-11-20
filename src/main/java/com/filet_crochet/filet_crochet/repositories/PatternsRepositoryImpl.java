package com.filet_crochet.filet_crochet.repositories;

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
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
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
    public PatternDto findById(String householdId, ObjectId id) {
        return mongoTemplate.findOne(new Query(byIdIn(householdId, id)), PatternDto.class, PATTERNS_COLLECTION);
    }

    @Override
    public boolean exists(String householdId, ObjectId id) {
        return mongoTemplate.exists(new Query(byIdIn(householdId, id)), PATTERNS_COLLECTION);
    }

    @Override
    public DeleteResult deleteById(String householdId, ObjectId id) {
        var query = new Query(byIdIn(householdId, id));
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
    public List<PatternDto> findAll(String householdId) {
        return mongoTemplate.find(new Query(inHousehold(householdId)), PatternDto.class, PATTERNS_COLLECTION);
    }

    @Override
    public PatternDto updatePattern(String householdId, ObjectId id, UpdatePatternDto pattern) {
        Query query = new Query(byIdIn(householdId, id));

        Update update = new Update();
        update.set("updatedAt", LocalDateTime.now());

        if (pattern.name() != null) {
            update.set("name", pattern.name());
        }

        if (pattern.cells() != null) {
            update.set("cells", pattern.cells());
        }

        if (pattern.sectionRowInterval() != null) {
            update.set("sectionRowInterval", pattern.sectionRowInterval());
        }

        if (pattern.sectionColInterval() != null) {
            update.set("sectionColInterval", pattern.sectionColInterval());
        }

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, PatternDto.class, PATTERNS_COLLECTION);
        if (updateResult.wasAcknowledged() && updateResult.getModifiedCount() > 0) {
            return findById(householdId, id);
        }
        return null;
    }

    @Override
    public PatternDto createPattern(PatternDto pattern) {
        return mongoTemplate.insert(pattern, PATTERNS_COLLECTION);
    }

    @Override
    public long deleteAll(String householdId) {
        // Progress first: it's found through the household's patterns.
        var ids = mongoTemplate.findDistinct(new Query(inHousehold(householdId)), "_id", PATTERNS_COLLECTION, ObjectId.class);
        long deleted = progressRepository.deleteAll(ids);
        return deleted + mongoTemplate.remove(new Query(inHousehold(householdId)), PATTERNS_COLLECTION).getDeletedCount();
    }

    private static Criteria inHousehold(String householdId) {
        return Criteria.where("householdId").is(new ObjectId(householdId));
    }

    private static Criteria byIdIn(String householdId, ObjectId id) {
        return Criteria.where("_id").is(id).and("householdId").is(new ObjectId(householdId));
    }
}
