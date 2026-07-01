package com.filet_crochet.filet_crochet.config;

import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
class ApplicationConfiguration {

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(@Value("${spring.data.mongodb.uri}") String uri, @Value("${spring.data.mongodb.database}") String database) {
        return new SimpleMongoClientDatabaseFactory(MongoClients.create(uri), database);
    }


}