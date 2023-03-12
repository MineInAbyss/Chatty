plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
    //id("com.mineinabyss.conventions.autoversion")
    kotlin("plugin.serialization")
}

repositories {
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
    compileOnly(libs.koin.core)

    // Other plugins
    compileOnly(chattyLibs.geary.papermc.core)

    /// Third-party plugins
    compileOnly(chattyLibs.placeholderapi)
    compileOnly(chattyLibs.discordsrv)

    // Shaded
    implementation(chattyLibs.imageloader)
    implementation(libs.bundles.idofront.core)
}
