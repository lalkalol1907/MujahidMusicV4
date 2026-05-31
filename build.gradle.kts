plugins {
    alias(libs.plugins.kotlin.jvm)
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

    implementation(libs.mongodb.driver.kotlin.sync)

    implementation(libs.dotenv.kotlin)
    implementation(libs.logback.classic)
}

kotlin {
    jvmToolchain(24)
}

application {
    mainClass.set("com.lalkalol.mujahid.MainKt")
}

tasks.shadowJar {
    archiveBaseName.set("MujahidMusicV4")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
}
