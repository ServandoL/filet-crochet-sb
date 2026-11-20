package com.filet_crochet.filet_crochet.repositories;

import com.filet_crochet.filet_crochet.dto.ProgressDto;
import com.filet_crochet.filet_crochet.dto.UpsertProgressDto;
import org.bson.types.ObjectId;

import java.util.List;

public interface ProgressRepository {
    public ProgressDto findById(ObjectId id);

    public ProgressDto upsertById(ObjectId id, UpsertProgressDto progressDto);

    public void deleteById(ObjectId id);
    /** For the purge. Returns how many it deleted. */
    public long deleteAll(List<ObjectId> patternIds);

}
