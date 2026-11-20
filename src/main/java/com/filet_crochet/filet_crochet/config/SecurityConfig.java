package com.filet_crochet.filet_crochet.config;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.HeaderBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Cloudflare Access signs people in at the edge and forwards a signed JWT in
 * {@code Cf-Access-Jwt-Assertion}. Every API request must carry a valid one, so a request that
 * skips Access gets 401. The {@code local} profile signs every request in as
 * {@code local.member-email} instead, for development.
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

        LocalDev(Environment environment) {
            if (environment.acceptsProfiles(Profiles.of("prod"))) {
                throw new IllegalStateException("The local profile skips sign-in; it can't run alongside prod.");
            }
        }

        /** Signs every request in as local.member-email, so the household check has someone to check. */
        @Bean
        @Order(2)
        SecurityFilterChain localChain(HttpSecurity http, @Value("${local.member-email}") String memberEmail)
                throws Exception {
            return statelessDefaults(http)
                    .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                    .addFilterBefore(new LocalMemberFilter(memberEmail), AnonymousAuthenticationFilter.class)
                    .build();
        }
    }

    static class LocalMemberFilter extends OncePerRequestFilter {

        private final String email;

        LocalMemberFilter(String email) {
            this.email = email;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated(email, null, AuthorityUtils.NO_AUTHORITIES));
            chain.doFilter(request, response);
        }
    }
}
