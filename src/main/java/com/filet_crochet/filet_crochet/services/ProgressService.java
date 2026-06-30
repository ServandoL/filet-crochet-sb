package com.filet_crochet.filet_crochet.services;

import com.filet_crochet.filet_crochet.dto.ProgressDto;
import com.filet_crochet.filet_crochet.dto.UpsertProgressDto;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProgressService {
    private static final Logger log = LoggerFactory.getLogger(ProgressService.class);
    private final MongoTemplate mongoTemplate;
    public static final String PROGRESS_COLLECTION = "PROGRESS";

    public ProgressService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Optional<ProgressDto> getById(String id) {
        try {
            var _id = new ObjectId(id);
            return Optional.ofNullable(mongoTemplate.findById(_id, ProgressDto.class, PROGRESS_COLLECTION));
        } catch (Exception e) {
            log.warn("Failed to get progress by id: {}. Error: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ProgressDto> upsertById(String id, UpsertProgressDto progress) {
        try {
            var _id = new ObjectId(id);
            var query = new Query().addCriteria(Criteria.where("_id").is(_id));
            var result = mongoTemplate.replace(query, progress, PROGRESS_COLLECTION);
            if (result.wasAcknowledged() && result.getModifiedCount() > 0) {
                return Optional.ofNullable(mongoTemplate.findById(_id, ProgressDto.class, PROGRESS_COLLECTION));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to upsert progress by id: {}. Error: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public void deleteById(String id) {
        try {
            var _id = new ObjectId(id);
            var query = new Query().addCriteria(Criteria.where("_id").is(_id));
            mongoTemplate.remove(query, PROGRESS_COLLECTION);
        } catch (Exception e) {
            log.warn("Failed to delete progress by id: {}. Error: {}", id, e.getMessage());
        }
    }
}
