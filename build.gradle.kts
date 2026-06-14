plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
    alias(libs.plugins.shadow)
}

group = "com.lalkalol"
version = "4.0.0"

repositories {
    mavenCentral()
    maven("https://maven.lavalink.dev/releases")
    maven("https://maven.lavalink.dev/snapshots")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlinx.coroutines.jdk8)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jda)
    implementation(libs.lavalink.client)
    implementation(libs.mongodb.driver.sync)
    implementation(libs.dotenv.java)
    implementation(libs.logback.classic)
    implementation(libs.micrometer.core)
    implementation(libs.micrometer.registry.prometheus)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.mockito.kotlin)
}

kotlin {
    jvmToolchain(24)
}

application {
    mainClass.set("com.lalkalol.mujahid.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("MujahidMusicV4")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
}

sourceSets {
    main {
        java.setSrcDirs(emptyList<String>())
        kotlin.setSrcDirs(listOf("src/main/kotlin"))
    }
    test {
        java.setSrcDirs(emptyList<String>())
        kotlin.setSrcDirs(listOf("src/test/kotlin"))
    }
}
