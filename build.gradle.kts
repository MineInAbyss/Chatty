plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
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
    implementation(project(":paper"))
    implementation(project(":velocity"))
    implementation(libs.idofront.core)
//    compileOnly(libs.idofront.nms)
}

val copyJar: String? by project
val pluginPath = project.findProperty("plugin_path")

if(copyJar != "false" && pluginPath != null) {
    tasks {
        println("Copying jar to $pluginPath")
        register<Copy>("copyJar") {
            from(findByName("reobfJar") ?: findByName("shadowJar") ?: findByName("jar"))
            rename("${project.name}-${project.version}.jar","${rootProject.name}-${project.name}-${project.version}.jar")
            into(pluginPath)
            doLast {
                println("Copied to plugin directory $pluginPath")
            }
        }

        named<DefaultTask>("build") {
            dependsOn("copyJar")
        }
    }
}
