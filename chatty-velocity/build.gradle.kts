plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    `maven-publish`
    id(idofrontLibs.plugins.mia.copyjar.get().pluginId)
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
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
//    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
    implementation(idofrontLibs.kotlinx.coroutines)
    implementation(idofrontLibs.kotlinx.serialization.json)
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("net.kyori:adventure-extra-kotlin:4.11.0")

    compileOnly(libs.velocity)
    kapt(libs.velocity)
}

copyJar {
    excludePlatformDependencies.set(false)
}
