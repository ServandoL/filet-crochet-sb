package com.filet_crochet.filet_crochet.services;

import com.filet_crochet.filet_crochet.repositories.PatternsRepositoryImpl;
import com.home.household.HouseholdDataPurger;
import org.springframework.stereotype.Component;

/**
 * Deletes a household's patterns and their progress once household-api's restore window for a
 * deleted household has passed. home-household-spring calls it daily for every such household,
 * so it does nothing once the data is gone. The *_BCK collections are manual backups and are
 * left alone.
 */
@Component
public class CrochetPurger implements HouseholdDataPurger {

    private final PatternsRepositoryImpl patterns;

    public CrochetPurger(PatternsRepositoryImpl patterns) {
        this.patterns = patterns;
    }

    @Override
    public long purge(String householdId) {
        return patterns.deleteAll(householdId);
    }
}
