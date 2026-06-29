package com.filet_crochet.filet_crochet.repositories;

import com.filet_crochet.filet_crochet.domains.Patterns;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PatternsRepository extends MongoRepository<Patterns, ObjectId> {

}
