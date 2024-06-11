plugins {
    kotlin("jvm") version "2.0.0"
    id("io.papermc.paperweight.userdev") version "1.7.1"
    kotlin("plugin.serialization") version "2.0.0"
    id("org.sonarqube") version "4.4.1.3373"
}

group = "com.liamxsage.boilerplates"
version = "2024.6.1"

val minecraftVersion: String by project
val slf4jVersion: String by project

val exposedVersion: String by project
val hikariCPVersion: String by project

val mysqlVersion: String by project
val mariaDBVersion: String by project
val sqliteVersion: String by project
val postgresqlVersion: String by project
val h2Version: String by project

val kotlinxSerializationJsonVersion: String by project

val fruxzAscendVersion: String by project
val fruxzStackedVersion: String by project

val kotlinxCoroutinesCoreVersion: String by project
val kotlinxCollectionsImmutableVersion: String by project

val gsonVersion: String by project

repositories {
    maven("https://nexus.flawcra.cc/repository/maven-mirrors/")
}

val deliverDependencies = listOf(
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesCoreVersion",
    "org.jetbrains.kotlinx:kotlinx-collections-immutable:$kotlinxCollectionsImmutableVersion",
    "org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion",
    "com.google.code.gson:gson:$gsonVersion",

    "dev.fruxz:ascend:$fruxzAscendVersion",
    "dev.fruxz:stacked:$fruxzStackedVersion",

    "org.jetbrains.exposed:exposed-core:$exposedVersion",
    "org.jetbrains.exposed:exposed-dao:$exposedVersion",
    "org.jetbrains.exposed:exposed-jdbc:$exposedVersion",
    "org.jetbrains.exposed:exposed-java-time:$exposedVersion",
    "com.zaxxer:HikariCP:$hikariCPVersion",

    // Driver for MySQL, MariaDB, Postgresql, H2 and SQLite
    "org.mariadb.jdbc:mariadb-java-client:$mariaDBVersion",
    "com.mysql:mysql-connector-j:$mysqlVersion",
    "org.xerial:sqlite-jdbc:$sqliteVersion",
    "org.postgresql:postgresql:$postgresqlVersion",
    "com.h2database:h2:$h2Version",

    "org.slf4j:slf4j-api:$slf4jVersion",
)

val includedDependencies = mutableListOf<String>()

fun Dependency?.deliver() = this?.apply {
    val computedVersion = version ?: kotlin.coreLibrariesVersion
    includedDependencies += "${group}:${name}:${computedVersion}"
}

dependencies {
    paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")

    implementation(kotlin("stdlib")).deliver()
    implementation(kotlin("reflect")).deliver()

    deliverDependencies.forEach { dependency ->
        implementation(dependency).deliver()
    }
}

tasks {
    build {
        dependsOn(reobfJar)
    }

    withType<ProcessResources> {
        expand(
            "version" to project.version,
            "name" to project.name,
            "dependencies" to includedDependencies.joinToString("\n")
        )
    }

    register<JavaCompile>("compileMain") {
        source = fileTree("src/main/java")
        classpath = files(configurations.runtimeClasspath)
        destinationDirectory.set(file("build/classes/kotlin/main"))
        options.release.set(21)
    }
}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.addAll(
            listOf(
                "-opt-in=kotlin.RequiresOptIn"
            )
        )
    }
}

sonar {
    properties {
        property("sonar.projectKey", "CoasterFreakDE_EnergeticStorage-Reborn_0578cff5-4b77-4f9d-8781-196fb92f58c7")
        property("sonar.projectName", "EnergeticStorage-Reborn")
    }
}