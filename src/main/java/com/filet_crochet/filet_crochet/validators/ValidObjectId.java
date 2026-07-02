package com.filet_crochet.filet_crochet.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ObjectIdValidator.class)
@Documented
@SuppressWarnings("unused")
public @interface ValidObjectId {
    String message() default "Invalid pattern ID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


