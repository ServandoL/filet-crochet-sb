package com.filet_crochet.filet_crochet.repositories;

import com.filet_crochet.filet_crochet.dto.ProgressDto;
import com.filet_crochet.filet_crochet.dto.UpsertProgressDto;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReplaceOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class ProgressRepositoryImpl implements ProgressRepository {
    public static final String PROGRESS_COLLECTION = "PROGRESS";
    private final MongoTemplate mongoTemplate;

    public ProgressRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public ProgressDto findById(ObjectId id) {
        return mongoTemplate.findById(id, ProgressDto.class, PROGRESS_COLLECTION);
    }

    @Override
    public ProgressDto upsertById(ObjectId id, UpsertProgressDto progressDto) {
        var query = new Query().addCriteria(Criteria.where("_id").is(id));
        var result = mongoTemplate.replace(query, progressDto, ReplaceOptions.replaceOptions().upsert(), PROGRESS_COLLECTION);
        if (result.wasAcknowledged() && result.getModifiedCount() > 0) {
            return mongoTemplate.findById(id, ProgressDto.class, PROGRESS_COLLECTION);
        }
        return null;
    }

    @Override
    public void deleteById(ObjectId id) {
        var query = new Query().addCriteria(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, PROGRESS_COLLECTION);
    }

}
