plugins {
    java
    alias(libs.plugins.shadow)
    application
}

group = "com.lalkalol"
version = "4.0.0"

repositories {
    mavenCentral()
    maven("https://maven.lavalink.dev/releases")
    maven("https://maven.lavalink.dev/snapshots")
}

dependencies {
    implementation(libs.jda)
    implementation(libs.lavalink.client)

    implementation(libs.mongodb.driver.sync)

    implementation(libs.dotenv.java)
    implementation(libs.logback.classic)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.junit.jupiter)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

application {
    mainClass.set("com.lalkalol.mujahid.Main")
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
