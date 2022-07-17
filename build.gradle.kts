plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
//    id("com.mineinabyss.conventions.nms")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.dokka")
}

dependencies {
    // MineInAbyss platform
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.kaml)
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.minecraft.mccoroutine)
    compileOnly(libs.koin.core)

    // Other plugins
    compileOnly(mylibs.geary.papermc.core)
    compileOnly(mylibs.guiy)

    // Shaded
    implementation(libs.idofront.core)
//    compileOnly(libs.idofront.nms)
}
