package com.filet_crochet.filet_crochet.config;

import com.filet_crochet.filet_crochet.repositories.PatternsRepositoryImpl;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
class ApplicationConfiguration {

    /**
     * The one client for MONGO_APP_URL. home-household-spring checks memberships through the
     * MongoClient bean; without this one Spring Boot would make its own, pointed at localhost.
     */
    @Bean
    public MongoClient mongoClient(@Value("${spring.data.mongodb.uri}") String uri) {
        return MongoClients.create(uri);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient client, @Value("${spring.data.mongodb.database}") String database) {
        return new SimpleMongoClientDatabaseFactory(client, database);
    }

    /**
     * Patterns are always looked up by household. Created at startup rather than through
     * auto-index creation (which is off). Tests without a database turn it off with
     * app.mongo.create-indexes=false.
     */
    @Bean
    @ConditionalOnProperty(name = "app.mongo.create-indexes", matchIfMissing = true)
    SmartInitializingSingleton indexInitializer(MongoTemplate mongo) {
        return () -> mongo.indexOps(PatternsRepositoryImpl.PATTERNS_COLLECTION)
                .createIndex(new Index().on("householdId", Sort.Direction.ASC).named("householdId"));
    }
}
