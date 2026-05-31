package com.lalkalol.mujahid.db

import com.mongodb.kotlin.client.MongoClient
import com.mongodb.kotlin.client.MongoDatabase
import org.slf4j.LoggerFactory

class MongoStorage(uri: String, databaseName: String) {
    private val log = LoggerFactory.getLogger(MongoStorage::class.java)
    private val client: MongoClient = MongoClient.create(uri)

    val database: MongoDatabase = client.getDatabase(databaseName)

    init {
        log.info("Connected to MongoDB database '{}'", databaseName)
    }

    fun close() {
        client.close()
    }
}
