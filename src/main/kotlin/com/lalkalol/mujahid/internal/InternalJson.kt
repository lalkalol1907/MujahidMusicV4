package com.lalkalol.mujahid.internal

import kotlinx.serialization.json.Json

object InternalJson {
    val json: Json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    inline fun <reified T> encode(value: T): String = json.encodeToString(value)
}
