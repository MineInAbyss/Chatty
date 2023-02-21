val kotlinVersion: String by project
val velocityVersion: String by project
val coroutinesVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    //id("com.mineinabyss.conventions.autoversion")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("net.kyori:adventure-extra-kotlin:4.11.0")

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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.shadowJar.get())
        }
    }
}
