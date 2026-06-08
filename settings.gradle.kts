rootProject.name = "chatty"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenLocal()
        google()
    }
//    includeBuild("../gradle-conventions")
}

dependencyResolutionManagement {
    val miaLibs: String by settings

    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }

    versionCatalogs {
        create("miaLibs") {
            from("com.mineinabyss:catalog:$miaLibs")
        }
    }
}

include(
    "chatty-paper",
    "chatty-velocity"
)

