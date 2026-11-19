package com.filet_crochet.filet_crochet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The Cloudflare Access application the API trusts: its team domain (e.g.
 * {@code yourteam.cloudflareaccess.com}) and AUD tag.
 */
@ConfigurationProperties("access")
public record AccessProperties(String teamDomain, String aud) {

    /** {@code https://<team domain>}; a full URL is used as given, which tests rely on. */
    public String issuer() {
        return teamDomain.contains("://") ? teamDomain : "https://" + teamDomain;
    }

    void requireConfigured() {
        if (teamDomain == null || teamDomain.isBlank() || aud == null || aud.isBlank()) {
            throw new IllegalStateException("Set ACCESS_TEAM_DOMAIN and ACCESS_AUD, or run with the local profile.");
        }
    }
}
