package com.filet_crochet.filet_crochet.validators;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PatternsValidator {
    private static final Logger log = LoggerFactory.getLogger(PatternsValidator.class);

    public boolean validateId(String id) {
        try {
            return !ObjectId.isValid(id);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid ObjectId: {}", id, e);
            return true;
        }
    }
}
