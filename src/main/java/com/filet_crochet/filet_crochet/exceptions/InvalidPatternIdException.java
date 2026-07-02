package com.filet_crochet.filet_crochet.exceptions;

public class InvalidPatternIdException extends RuntimeException {
    public InvalidPatternIdException(String id) {
        super("Invalid pattern ID: " + id);
    }
}

