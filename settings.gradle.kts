rootProject.name = "chatty"

pluginManagement {

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
        google()
    }
}

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

include(
    "chatty-paper",
    "chatty-velocity"
)
