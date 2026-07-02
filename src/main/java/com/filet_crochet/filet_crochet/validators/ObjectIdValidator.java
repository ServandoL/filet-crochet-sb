package com.filet_crochet.filet_crochet.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectIdValidator implements ConstraintValidator<ValidObjectId, String> {
    private static final Logger log = LoggerFactory.getLogger(ObjectIdValidator.class);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Only validate if value is present; use @NotNull for null checks
        if (value == null) {
            return true;
        }
        try {
            return ObjectId.isValid(value);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid ObjectId: {}", value, e);
            return false;
        }
    }
}

