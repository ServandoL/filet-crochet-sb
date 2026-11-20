package com.filet_crochet.filet_crochet.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import com.filet_crochet.filet_crochet.services.PatternsService;
import com.home.household.CurrentMember;
import com.home.household.HouseholdFilter;
import com.home.household.HouseholdMemberships;
import com.filet_crochet.filet_crochet.services.ProgressService;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Real token verification, against a local JWKS endpoint standing in for Cloudflare Access. The
 * services are mocked, so no database is needed.
 */
@SpringBootTest(properties = {"MONGO_APP_URL=mongodb://localhost:1/test", "MONGO_DATABASE=test", "SERVER_PORT=0",
        "app.mongo.create-indexes=false"})
@AutoConfigureMockMvc
class AccessJwtTests {

    private static final String HEADER = "Cf-Access-Jwt-Assertion";

    private static final String AUDIENCE = "test-aud-tag";

    private static final String READ = "/api/patterns";
    private static final String HOUSEHOLD = "6afe67e6c0a8bf150c541825";

    private static final RSAKey KEY = generateKey();

    private static final HttpServer JWKS = startJwks(new JWKSet(KEY.toPublicJWK()));

    private static final String ISSUER = "http://localhost:" + JWKS.getAddress().getPort();

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PatternsService patternsService;

    @MockitoBean
    private ProgressService progressService;

    /** Every signed-in email belongs to the household, so only the security checks decide. */
    @MockitoBean
    private HouseholdMemberships memberships;

    @BeforeEach
    void everyoneIsAMember() {
        when(memberships.find(anyString(), anyString())).thenAnswer(
                call -> Optional.of(new CurrentMember(call.getArgument(0), call.getArgument(1), CurrentMember.Role.MEMBER)));
    }

    private static AbstractMockHttpServletRequestBuilder<?> read() {
        return get(READ).header(HouseholdFilter.HEADER, HOUSEHOLD);
    }

    @DynamicPropertySource
    static void accessProperties(DynamicPropertyRegistry registry) {
        registry.add("access.team-domain", () -> ISSUER);
        registry.add("access.aud", () -> AUDIENCE);
    }

    @AfterAll
    static void stopJwks() {
        JWKS.stop(0);
    }

    /** A write the API would accept, so only the security checks decide the outcome. */
    private static AbstractMockHttpServletRequestBuilder<?> write() {
        return delete("/api/patterns/507f1f77bcf86cd799439011/progress").header(HouseholdFilter.HEADER, HOUSEHOLD);
    }

    @Test
    void signedTokenIsAccepted() throws Exception {
        mvc.perform(read().header(HEADER, token(KEY, claims -> {
        }))).andExpect(status().isOk());
    }

    @Test
    void audienceListThatIncludesOursIsAccepted() throws Exception {
        mvc.perform(read().header(HEADER, token(KEY, claims -> claims.audience(List.of("another-app", AUDIENCE)))))
                .andExpect(status().isOk());
    }

    @Test
    void missingHeaderIsRejected() throws Exception {
        mvc.perform(read()).andExpect(status().isUnauthorized());
    }

    @Test
    void authorizationHeaderIsNotRead() throws Exception {
        mvc.perform(read().header(HttpHeaders.AUTHORIZATION, "Bearer " + token(KEY, claims -> {
        }))).andExpect(status().isUnauthorized());
    }

    @Test
    void malformedTokenIsRejected() throws Exception {
        expectRejected("not-a-jwt");
    }

    @Test
    void wrongAudienceIsRejected() throws Exception {
        expectRejected(token(KEY, claims -> claims.audience("another-app")));
    }

    @Test
    void wrongIssuerIsRejected() throws Exception {
        expectRejected(token(KEY, claims -> claims.issuer("https://evil.cloudflareaccess.com")));
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        expectRejected(token(KEY, claims -> claims.expirationTime(Date.from(Instant.now().minusSeconds(3600)))));
    }

    @Test
    void unknownKeyIsRejected() throws Exception {
        expectRejected(token(generateKey(), claims -> {
        }));
    }

    @Test
    void missingEmailIsRejected() throws Exception {
        expectRejected(token(KEY, claims -> claims.claim("email", null)));
    }

    @Test
    void writeWithoutTokenIsRejected() throws Exception {
        mvc.perform(write().header("Sec-Fetch-Site", "same-origin")).andExpect(status().isUnauthorized());
    }

    @Test
    void sameOriginWriteIsAllowed() throws Exception {
        expectAllowed(mvc.perform(write().header(HEADER, token(KEY, claims -> {
        })).header("Sec-Fetch-Site", "same-origin")));
    }

    @Test
    void writeWithoutSecFetchSiteIsAllowed() throws Exception {
        expectAllowed(mvc.perform(write().header(HEADER, token(KEY, claims -> {
        }))));
    }

    // nginx reaches the API over plain HTTP, but the browser's Origin is https. Unless the API
    // reads the forwarded headers, it takes a same-origin save for a CORS request and rejects it.
    @Test
    void sameOriginWriteThroughTheProxyIsAllowed() throws Exception {
        expectAllowed(mvc.perform(write().header(HEADER, token(KEY, claims -> {
                }))
                .header("Sec-Fetch-Site", "same-origin")
                .header("Origin", "https://home.ksl-apps.dev")
                .header("Host", "home.ksl-apps.dev")
                .header("X-Forwarded-Proto", "https")
                .header("X-Forwarded-Host", "home.ksl-apps.dev")
                .header("X-Forwarded-Port", "443")));
    }

    @Test
    void crossSiteWriteIsForbidden() throws Exception {
        mvc.perform(write().header(HEADER, token(KEY, claims -> {
        })).header("Sec-Fetch-Site", "cross-site")).andExpect(status().isForbidden());
    }

    @Test
    void sameSiteWriteFromAnotherSubdomainIsForbidden() throws Exception {
        mvc.perform(write().header(HEADER, token(KEY, claims -> {
        })).header("Sec-Fetch-Site", "same-site")).andExpect(status().isForbidden());
    }

    // There's no database here, so health reports DOWN (503); it just mustn't ask for a token.
    @Test
    void healthNeedsNoToken() throws Exception {
        expectAllowed(mvc.perform(get("/actuator/health")));
    }

    private void expectRejected(String token) throws Exception {
        mvc.perform(read().header(HEADER, token)).andExpect(status().isUnauthorized());
    }

    private static void expectAllowed(ResultActions result) {
        int status = result.andReturn().getResponse().getStatus();
        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }

    private static String token(RSAKey key, Consumer<JWTClaimsSet.Builder> customizer) throws JOSEException {
        Instant now = Instant.now();
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().issuer(ISSUER)
                .audience(List.of(AUDIENCE))
                .subject(UUID.randomUUID().toString())
                .claim("email", "ana@example.com")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(600)));
        customizer.accept(claims);
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),
                claims.build());
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    private static RSAKey generateKey() {
        try {
            return new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
        } catch (JOSEException ex) {
            throw new IllegalStateException(ex);
        }
    }

    // Serves the JWKS where Cloudflare does: <team domain>/cdn-cgi/access/certs
    private static HttpServer startJwks(JWKSet keys) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            byte[] body = keys.toString().getBytes(StandardCharsets.UTF_8);
            server.createContext("/cdn-cgi/access/certs", exchange -> {
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
