plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    `maven-publish`
    id(miaLibs.plugins.mia.copyjar.get().pluginId)
}

copyJar {
    destPath = project.findProperty("velocity_plugin_path") as String? ?: "./build/publish"
    generatePluginYml = false
    excludePlatformDependencies = false
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenLocal()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(miaLibs.kotlinx.coroutines)
    implementation(miaLibs.kotlinx.serialization.json)
    implementation(miaLibs.kotlinx.serialization.kaml)
    implementation("net.kyori:adventure-extra-kotlin:4.11.0")

    compileOnly(libs.velocity)
    kapt(libs.velocity)
}
