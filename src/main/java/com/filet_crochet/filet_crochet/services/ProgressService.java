package com.filet_crochet.filet_crochet.services;

import com.filet_crochet.filet_crochet.dto.ProgressDto;
import com.filet_crochet.filet_crochet.dto.UpsertProgressDto;
import com.filet_crochet.filet_crochet.repositories.PatternsRepositoryImpl;
import com.filet_crochet.filet_crochet.repositories.ProgressRepositoryImpl;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProgressService {
    private static final Logger log = LoggerFactory.getLogger(ProgressService.class);
    @Autowired
    private final ProgressRepositoryImpl progressRepository;
    private final PatternsRepositoryImpl patternsRepository;

    /** Progress belongs to a household through its pattern: every call checks the pattern first. */
    public ProgressService(ProgressRepositoryImpl progressRepository, PatternsRepositoryImpl patternsRepository) {
        this.progressRepository = progressRepository;
        this.patternsRepository = patternsRepository;
    }

    public Optional<ProgressDto> getById(String householdId, String id) {
        try {
            if (!patternsRepository.exists(householdId, new ObjectId(id))) {
                return Optional.empty();
            }
            return Optional.ofNullable(progressRepository.findById(new ObjectId(id)));
        } catch (Exception e) {
            log.warn("Failed to get progress by id: {}. Error: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ProgressDto> upsertById(String householdId, String id, UpsertProgressDto progress) {
        try {
            if (!patternsRepository.exists(householdId, new ObjectId(id))) {
                return Optional.empty();
            }
            return Optional.ofNullable(progressRepository.upsertById(new ObjectId(id), progress));
        } catch (Exception e) {
            log.warn("Failed to upsert progress by id: {}. Error: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public void deleteById(String householdId, String id) {
        try {
            if (!patternsRepository.exists(householdId, new ObjectId(id))) {
                return;
            }
            progressRepository.deleteById(new ObjectId(id));
        } catch (Exception e) {
            log.warn("Failed to delete progress by id: {}. Error: {}", id, e.getMessage());
        }
    }
}
