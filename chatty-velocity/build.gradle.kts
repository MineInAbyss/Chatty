val kotlinVersion: String by project
val velocityVersion: String by project
val coroutinesVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    `maven-publish`
    id(libs.plugins.mia.copyjar.get().pluginId)
}

copyJar {
    destPath.set(project.findProperty("velocity_plugin_path") as String? ?: "./build/publish")
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
    kapt(chattyLibs.velocity)
}
