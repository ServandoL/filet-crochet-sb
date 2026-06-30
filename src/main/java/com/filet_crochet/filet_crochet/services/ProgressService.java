package com.filet_crochet.filet_crochet.services;

import com.filet_crochet.filet_crochet.dto.ProgressDto;
import com.filet_crochet.filet_crochet.dto.UpsertProgressDto;
import com.filet_crochet.filet_crochet.repositories.ProgressRepositoryImpl;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProgressService {
    private static final Logger log = LoggerFactory.getLogger(ProgressService.class);
    private final ProgressRepositoryImpl progressRepository;

    public ProgressService(ProgressRepositoryImpl progressRepository) {
        this.progressRepository = progressRepository;
    }

    public Optional<ProgressDto> getById(String id) {
        try {
            return Optional.ofNullable(progressRepository.findById(new ObjectId(id)));
        } catch (Exception e) {
            log.warn("Failed to get progress by id: {}. Error: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ProgressDto> upsertById(String id, UpsertProgressDto progress) {
        try {
            return Optional.ofNullable(progressRepository.upsertById(new ObjectId(id), progress));
        } catch (Exception e) {
            log.warn("Failed to upsert progress by id: {}. Error: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public void deleteById(String id) {
        try {
            progressRepository.deleteById(new ObjectId(id));
        } catch (Exception e) {
            log.warn("Failed to delete progress by id: {}. Error: {}", id, e.getMessage());
        }
    }
}
