package com.lalkalol.mujahid.db;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoStorage {
    private static final Logger log = LoggerFactory.getLogger(MongoStorage.class);

    private final MongoClient client;
    private final MongoDatabase database;

    public MongoStorage(String uri, String databaseName) {
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        client = MongoClients.create(uri);
        database = client.getDatabase(databaseName).withCodecRegistry(codecRegistry);
        log.info("Connected to MongoDB database '{}'", databaseName);
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void close() {
        client.close();
    }
}
