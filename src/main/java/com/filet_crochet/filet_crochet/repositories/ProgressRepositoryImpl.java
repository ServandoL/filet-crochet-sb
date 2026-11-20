package com.filet_crochet.filet_crochet.repositories;

import com.filet_crochet.filet_crochet.dto.ProgressDto;
import com.filet_crochet.filet_crochet.dto.UpsertProgressDto;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReplaceOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
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
        var result = mongoTemplate.replace(query, new ProgressDto(LocalDateTime.now(), progressDto.highlightedCells()), ReplaceOptions.replaceOptions().upsert(), PROGRESS_COLLECTION);
        // The first save for a pattern inserts, so nothing is "modified"; count that as saved too.
        if (result.wasAcknowledged() && (result.getMatchedCount() > 0 || result.getUpsertedId() != null)) {
            return mongoTemplate.findById(id, ProgressDto.class, PROGRESS_COLLECTION);
        }
        return null;
    }

    @Override
    public void deleteById(ObjectId id) {
        var query = new Query().addCriteria(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, PROGRESS_COLLECTION);
    }

    @Override
    public long deleteAll(List<ObjectId> patternIds) {
        return mongoTemplate.remove(new Query(Criteria.where("_id").in(patternIds)), PROGRESS_COLLECTION).getDeletedCount();
    }

}
