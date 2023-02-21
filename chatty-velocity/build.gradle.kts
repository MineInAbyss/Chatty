val kotlinVersion: String by project
val velocityVersion: String by project
val coroutinesVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    //id("com.mineinabyss.conventions.autoversion")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // MineInAbyss platform
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.kaml)
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.minecraft.mccoroutine)

    compileOnly(chattyLibs.velocity)
}

tasks.build {
    dependsOn(tasks.shadowJar.get())
}

val copyJar: String? by project
val pluginPath = project.findProperty("velocity_plugin_path")

if(copyJar != "false" && pluginPath != null) {
    tasks {
        register<Copy>("copyJar") {
            from(findByName("reobfJar") ?: findByName("shadowJar") ?: findByName("jar"))
            into(pluginPath)
            doLast {
                println("Copied to plugin directory $pluginPath")
            }
        }

        named<DefaultTask>("build") {
            dependsOn("copyJar")
        }
    }
}
