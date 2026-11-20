package com.filet_crochet.filet_crochet.repositories;

import com.filet_crochet.filet_crochet.dto.PatternDto;
import com.filet_crochet.filet_crochet.dto.UpdatePatternDto;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;

import java.util.List;

/** Every method takes the household, so a pattern in another household is never found. */
public interface PatternsRepository {
    public PatternDto findById(String householdId, ObjectId id);
    public boolean exists(String householdId, ObjectId id);
    public DeleteResult deleteById(String householdId, ObjectId id);
    public List<PatternDto> findAll(String householdId);
    public PatternDto updatePattern(String householdId, ObjectId id, UpdatePatternDto pattern);
    public PatternDto createPattern(PatternDto pattern);
    /** For the purge: deletes the household's patterns and their progress. Returns how many documents. */
    public long deleteAll(String householdId);
}
