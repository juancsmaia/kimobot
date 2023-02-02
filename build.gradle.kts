
plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version("7.1.2")
    id("java")
    application
}

application {
    mainClass.set("org.kimobot.kimo.KimoBotApplication")
}

group = "org.kimobot"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://m2.dv8tion.net/releases") // jda
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.22") {
        exclude(module="opus-java")
    }

    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")
    implementation("mysql:mysql-connector-java:8.0.27")
    implementation("commons-dbcp:commons-dbcp:1.4")
    implementation("commons-dbutils:commons-dbutils:1.7")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")

    implementation("org.flywaydb:flyway-core:8.0.4")
    implementation("org.json:json:20180130")
}