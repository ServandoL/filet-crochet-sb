package com.filet_crochet.filet_crochet.repositories;

import com.filet_crochet.filet_crochet.domains.Progress;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProgressRepository extends MongoRepository<Progress, ObjectId> {
}
