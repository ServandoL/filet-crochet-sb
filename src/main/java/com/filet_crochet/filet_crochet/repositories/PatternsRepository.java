package com.filet_crochet.filet_crochet.repositories;

import com.filet_crochet.filet_crochet.dto.PatternDto;
import com.filet_crochet.filet_crochet.dto.UpdatePatternDto;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;

import java.util.List;

public interface PatternsRepository {
    public PatternDto findById(ObjectId id);

    public DeleteResult deleteById(ObjectId id);

    public List<PatternDto> findAll();

    public PatternDto updatePattern(ObjectId id, UpdatePatternDto pattern);

    public PatternDto createPattern(PatternDto pattern);
}
