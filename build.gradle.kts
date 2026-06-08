plugins {
    alias(miaLibs.plugins.mia.kotlin.jvm)
    alias(miaLibs.plugins.kotlinx.serialization)
    alias(miaLibs.plugins.mia.papermc)
    alias(miaLibs.plugins.mia.publication)
    alias(miaLibs.plugins.mia.autoversion)
}

dependencies {
    // MineInAbyss platform
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
    compileOnly(miaLibs.kotlinx.coroutines)
    compileOnly(miaLibs.minecraft.mccoroutine)
}
