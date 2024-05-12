plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    `maven-publish`
    id(idofrontLibs.plugins.mia.copyjar.get().pluginId)
}

copyJar {
    destPath.set(project.findProperty("velocity_plugin_path") as String? ?: "./build/publish")
    excludePlatformDependencies.set(false)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenLocal()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(idofrontLibs.kotlinx.coroutines)
    implementation(idofrontLibs.kotlinx.serialization.json)
    implementation("net.kyori:adventure-extra-kotlin:4.11.0")

    compileOnly(libs.velocity)
    kapt(libs.velocity)
}
