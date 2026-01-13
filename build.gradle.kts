import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.toplugins"
version = "1.0.5"

repositories {
    mavenCentral()

    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/public/")
    maven(url = "https://repo.md-5.net/content/repositories/snapshots/")
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven(url = "https://jitpack.io")
}

dependencies {

    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("mysql:mysql-connector-java:8.0.33")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("ToEconomy")
    archiveClassifier.set("")

    relocate("com.zaxxer.hikari", "com.toplugins.toeconomy.libs.hikari")
    relocate("com.mysql", "com.toplugins.toeconomy.libs.mysql")
}

tasks.build {
    dependsOn(tasks.named("shadowJar"))
}

tasks.jar {
    enabled = false
}
