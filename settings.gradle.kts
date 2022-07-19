pluginManagement {
    val kotlinVersion: String by settings
    val idofrontVersion: String by settings

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
        google()
    }

    plugins {

        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion

        id("com.github.johnrengelman.shadow") version "6.1.0"
    }

    resolutionStrategy.eachPlugin {
        if (requested.id.id.startsWith("com.mineinabyss.conventions")) useVersion(idofrontVersion)
    }
}

include(
    "chatty-paper",
    "chatty-velocity"
)

dependencyResolutionManagement {
    val idofrontVersion: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
    }

    versionCatalogs {
        create("libs").from("com.mineinabyss:catalog:$idofrontVersion")
        create("chattyLibs").from(files("gradle/chattyLibs.versions.toml"))
    }
}

rootProject.name = "chatty"
