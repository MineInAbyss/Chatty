import net.minecrell.pluginyml.paper.PaperPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder.BEFORE

plugins {
    id(miaLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(miaLibs.plugins.mia.papermc.get().pluginId)
    id(miaLibs.plugins.mia.publication.get().pluginId)
    id(miaLibs.plugins.mia.copyjar.get().pluginId)
    alias(miaLibs.plugins.kotlinx.serialization)
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
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
    compileOnly(miaLibs.kotlinx.coroutines)
    compileOnly(miaLibs.minecraft.mccoroutine)
    compileOnly(miaLibs.bundles.idofront.core)

    // Other plugins
    compileOnly(miaLibs.geary.papermc)

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
        )
    }
}

paper {
    name = "Chatty"
    main = "com.mineinabyss.chatty.ChattyPlugin"
    author = "boy0000"
    description = "Highly customizable chat plugin"
    serverDependencies {
        register("Geary") {
            required = true
            load = BEFORE
            joinClasspath = true
        }
        register("PlaceholderAPI") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("DiscordSRV") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("Emojy") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
    }
}