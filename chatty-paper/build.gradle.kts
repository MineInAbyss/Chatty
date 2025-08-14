plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.papermc.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    id(idofrontLibs.plugins.mia.copyjar.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/snapshots")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
    mavenLocal()
}

dependencies {
    // MineInAbyss platform
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.kaml)
    compileOnly(idofrontLibs.kotlinx.coroutines)
    compileOnly(idofrontLibs.minecraft.mccoroutine)
    compileOnly(idofrontLibs.bundles.idofront.core)

    // Other plugins
    compileOnly(libs.geary.papermc)

    /// Third-party plugins
    compileOnly(libs.placeholderapi)
    compileOnly(libs.discordsrv)

}

configurations {
    findByName("runtimeClasspath")?.apply {
        exclude(group = "org.jetbrains.kotlin")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlin.ExperimentalUnsignedTypes",
            "-Xcontext-receivers"
        )
    }
}
