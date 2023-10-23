@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.mia.kotlin.jvm)
    alias(libs.plugins.mia.papermc)
    alias(libs.plugins.mia.testing)
    alias(libs.plugins.mia.publication)
    id(libs.plugins.mia.copyjar.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/snapshots")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
    maven("https://m2.dv8tion.net/releases") // DiscordSRV
}

dependencies {
    // MineInAbyss platform
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.kaml)
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.minecraft.mccoroutine)
    compileOnly(libs.bundles.idofront.core)

    // Other plugins
    compileOnly(chattyLibs.geary.papermc)

    /// Third-party plugins
    compileOnly(chattyLibs.placeholderapi)
    compileOnly(chattyLibs.discordsrv)

    // Shaded
    implementation(chattyLibs.imageloader)

}

configurations {
    findByName("runtimeClasspath")?.apply {
        exclude(group = "org.jetbrains.kotlin")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}
