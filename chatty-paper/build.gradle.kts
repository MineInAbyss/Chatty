plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
    kotlin("plugin.serialization")
}

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
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
    compileOnly(chattyLibs.bondrewdlikeshisemotes)

    /// Third-party plugins
    compileOnly(chattyLibs.placeholderapi)

    // Shaded
    implementation(libs.idofront.core)
//    compileOnly(libs.idofront.nms)
}
