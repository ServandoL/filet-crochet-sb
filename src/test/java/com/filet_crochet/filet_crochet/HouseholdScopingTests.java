package com.filet_crochet.filet_crochet;

import com.filet_crochet.filet_crochet.services.CrochetPurger;
import com.home.household.HouseholdFilter;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Against a real MongoDB: one household's patterns and their progress are invisible to another,
 * through the API and the purge.
 */
@SpringBootTest(properties = {"MONGO_DATABASE=CROCHET", "household.database=HOUSEHOLD",
        "access.team-domain=test.cloudflareaccess.com", "access.aud=test-aud"})
@AutoConfigureMockMvc
@Testcontainers
class HouseholdScopingTests {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0");

    // ApplicationConfiguration builds the MongoClient from this, not from a service connection.
    @DynamicPropertySource
    static void mongo(DynamicPropertyRegistry registry) {
        registry.add("MONGO_APP_URL", MONGO::getConnectionString);
    }

    static final String SAM = "sam@example.com";
    static final String KIM = "kim@example.com";

    @Autowired
    MockMvc mvc;

    @Autowired
    MongoClient client;

    @Autowired
    MongoTemplate mongo;

    @Autowired
    ApplicationContext context;

    @Autowired
    CrochetPurger purger;

    String samHome;
    String kimHome;

    @BeforeEach
    void households() {
        client.getDatabase("HOUSEHOLD").getCollection("households").deleteMany(new Document());
        for (String collection : mongo.getCollectionNames()) {
            mongo.getCollection(collection).deleteMany(new Document());
        }
        samHome = household(SAM);
        kimHome = household(KIM);
    }

    @Test
    void theHouseholdCheckReadsTheConfiguredMongo() {
        // Otherwise Spring Boot adds its own MongoClient for localhost, and the check reads that.
        assertThat(context.getBeanNamesForType(MongoClient.class)).hasSize(1);
    }

    @Test
    void eachHouseholdSeesOnlyItsOwnPatterns() throws Exception {
        String rose = id(createPattern(SAM, samHome, "Rose").andExpect(status().isCreated())
                .andExpect(jsonPath("$.householdId").doesNotExist()));

        patterns(SAM, samHome).andExpect(jsonPath("$[*].name", contains("Rose")));
        patterns(KIM, kimHome).andExpect(jsonPath("$", empty()));

        Document stored = mongo.getCollection("PATTERNS").find().first();
        assertThat(stored.getObjectId("householdId")).isEqualTo(new ObjectId(samHome));
        assertThat(stored.getObjectId("_id").toHexString()).isEqualTo(rose);
    }

    @Test
    void anotherHouseholdsPatternCantBeReadChangedOrDeleted() throws Exception {
        String rose = id(createPattern(SAM, samHome, "Rose"));

        mvc.perform(get("/api/patterns/" + rose).with(as(KIM, kimHome))).andExpect(status().isNotFound());
        mvc.perform(json(patch("/api/patterns/" + rose), "{\"name\":\"Mine\"}").with(as(KIM, kimHome)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/patterns/" + rose).with(as(KIM, kimHome))).andExpect(status().isNotFound());

        mvc.perform(get("/api/patterns/" + rose).with(as(SAM, samHome)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rose"));
    }

    @Test
    void progressFollowsItsPatternsHousehold() throws Exception {
        String rose = id(createPattern(SAM, samHome, "Rose"));
        // Used to answer 404: the first save inserts, and nothing counted as "modified".
        saveProgress(SAM, samHome, rose).andExpect(status().isOk())
                .andExpect(jsonPath("$.highlightedCells[0][0]").value(1));

        mvc.perform(get("/api/patterns/" + rose + "/progress").with(as(KIM, kimHome))).andExpect(status().isNotFound());
        saveProgress(KIM, kimHome, rose).andExpect(status().isNotFound());
        mvc.perform(delete("/api/patterns/" + rose + "/progress").with(as(KIM, kimHome)));

        mvc.perform(get("/api/patterns/" + rose + "/progress").with(as(SAM, samHome))).andExpect(status().isOk());
    }

    @Test
    void someoneOutsideTheHouseholdIsRefused() throws Exception {
        patterns(KIM, samHome).andExpect(status().isForbidden());
        mvc.perform(get("/api/patterns").with(jwt().jwt(j -> j.claim("email", SAM)))).andExpect(status().is(428));
    }

    @Test
    void thePurgeDeletesOneHouseholdsPatternsAndProgressOnly() throws Exception {
        String rose = id(createPattern(SAM, samHome, "Rose"));
        saveProgress(SAM, samHome, rose);
        String tulip = id(createPattern(KIM, kimHome, "Tulip"));
        saveProgress(KIM, kimHome, tulip);
        mongo.getCollection("PATTERNS_BCK").insertOne(new Document("name", "Backup"));

        assertThat(purger.purge(samHome)).isEqualTo(2);
        assertThat(purger.purge(samHome)).isZero();

        patterns(SAM, samHome).andExpect(jsonPath("$", empty()));
        patterns(KIM, kimHome).andExpect(jsonPath("$[*].name", contains("Tulip")));
        assertThat(mongo.getCollection("PROGRESS").countDocuments()).isEqualTo(1);
        assertThat(mongo.getCollection("PATTERNS_BCK").countDocuments()).isEqualTo(1);
    }

    @Test
    void everyWriteTellsTheHouseholdsOtherDevices() throws Exception {
        MvcResult samsStream = mvc.perform(get("/api/v1/events").with(as(SAM, samHome)))
                .andExpect(request().asyncStarted()).andReturn();
        MvcResult kimsStream = mvc.perform(get("/api/v1/events").with(as(KIM, kimHome)))
                .andExpect(request().asyncStarted()).andReturn();

        String rose = id(createPattern(SAM, samHome, "Rose"));
        mvc.perform(json(patch("/api/patterns/" + rose), "{\"name\":\"Roses\"}").with(as(SAM, samHome)))
                .andExpect(status().isOk());
        saveProgress(SAM, samHome, rose).andExpect(status().isOk());
        mvc.perform(delete("/api/patterns/" + rose + "/progress").with(as(SAM, samHome)))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/patterns/" + rose).with(as(SAM, samHome))).andExpect(status().isNoContent());

        assertThat(samsStream.getResponse().getContentAsString())
                .contains("data:{\"resource\":\"patterns\",\"action\":\"created\",\"id\":\"" + rose + "\"}")
                .contains("data:{\"resource\":\"patterns\",\"action\":\"updated\",\"id\":\"" + rose + "\"}")
                .contains("data:{\"resource\":\"progress\",\"action\":\"updated\",\"id\":\"" + rose + "\"}")
                .contains("data:{\"resource\":\"progress\",\"action\":\"deleted\",\"id\":\"" + rose + "\"}")
                .contains("data:{\"resource\":\"patterns\",\"action\":\"deleted\",\"id\":\"" + rose + "\"}");
        assertThat(kimsStream.getResponse().getContentAsString()).doesNotContain("event:change");
    }

    private ResultActions createPattern(String email, String household, String name) throws Exception {
        return mvc.perform(json(post("/api/patterns"),
                "{\"name\":\"" + name + "\",\"rows\":2,\"cols\":2,\"cells\":[[0,1],[1,0]]}").with(as(email, household)));
    }

    private ResultActions patterns(String email, String household) throws Exception {
        return mvc.perform(get("/api/patterns").with(as(email, household)));
    }

    private ResultActions saveProgress(String email, String household, String patternId) throws Exception {
        return mvc.perform(json(put("/api/patterns/" + patternId + "/progress"), "{\"highlightedCells\":[[1,0]]}")
                .with(as(email, household)));
    }

    private static String id(ResultActions created) throws Exception {
        return JsonPath.read(created.andReturn().getResponse().getContentAsString(), "$._id");
    }

    private static RequestPostProcessor as(String email, String household) {
        var token = jwt().jwt(j -> j.claim("email", email));
        return request -> {
            request.addHeader(HouseholdFilter.HEADER, household);
            return token.postProcessRequest(request);
        };
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body).header("Sec-Fetch-Site", "same-origin");
    }

    private String household(String email) {
        ObjectId id = new ObjectId();
        client.getDatabase("HOUSEHOLD").getCollection("households").insertOne(new Document("_id", id)
                .append("name", "Home")
                .append("members", List.of(new Document("email", email).append("role", "ADMIN")))
                .append("deletedAt", null));
        return id.toHexString();
    }
}
