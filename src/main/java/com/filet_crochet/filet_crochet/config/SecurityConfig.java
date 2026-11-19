package com.filet_crochet.filet_crochet.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.HeaderBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cloudflare Access signs people in at the edge and forwards a signed JWT in
 * {@code Cf-Access-Jwt-Assertion}. Every API request must carry a valid one, so a request that
 * skips Access gets 401. The {@code local} profile turns the check off for development.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AccessProperties.class)
public class SecurityConfig {

    static final String ACCESS_JWT_HEADER = "Cf-Access-Jwt-Assertion";

    /** The compose healthcheck calls /actuator/health on the API port without a token. */
    @Bean
    @Order(1)
    SecurityFilterChain healthChain(HttpSecurity http) throws Exception {
        return http.securityMatcher(EndpointRequest.to("health"))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .build();
    }

    private static HttpSecurity statelessDefaults(HttpSecurity http) {
        return http.cors(Customizer.withDefaults()) // keeps the WebMvcConfigurer CORS rules
                .csrf(c -> c.disable()) // stateless; SameSiteWritesFilter blocks cross-site writes
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("!local")
    static class AccessJwt {

        @Bean
        @Order(2)
        SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
            return statelessDefaults(http)
                    .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                    .oauth2ResourceServer(o -> o.bearerTokenResolver(new HeaderBearerTokenResolver(ACCESS_JWT_HEADER))
                            .jwt(Customizer.withDefaults()))
                    .build();
        }

        /** Access's signing keys, its issuer, this application's AUD tag, and an email. */
        @Bean
        JwtDecoder accessJwtDecoder(AccessProperties access) {
            access.requireConfigured();
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(access.issuer() + "/cdn-cgi/access/certs").build();
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                    JwtValidators.createDefaultWithIssuer(access.issuer()),
                    new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains(access.aud())),
                    new JwtClaimValidator<Object>("email", email -> email instanceof String s && !s.isBlank())));
            return decoder;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("local")
    static class LocalDev {

        @Bean
        @Order(2)
        SecurityFilterChain localChain(HttpSecurity http) throws Exception {
            return statelessDefaults(http)
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                    .build();
        }
    }
}
