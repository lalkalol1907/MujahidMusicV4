package com.lalkalol.mujahid.db

import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.slf4j.LoggerFactory

class MongoStorage(uri: String, databaseName: String) {
    private val log = LoggerFactory.getLogger(MongoStorage::class.java)
    private val client: MongoClient
    val database: MongoDatabase

    init {
        val codecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()),
        )
        client = MongoClients.create(uri)
        database = client.getDatabase(databaseName).withCodecRegistry(codecRegistry)
        log.info("Connected to MongoDB database '{}'", databaseName)
    }

    fun close() {
        client.close()
    }
}
