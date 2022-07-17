package com.mineinabyss.chatty

import com.mineinabyss.chatty.listeners.ChatListener
import com.mineinabyss.chatty.listeners.PlayerListener
import com.mineinabyss.geary.addon.autoscan
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.config.singleConfig
import com.mineinabyss.idofront.config.startOrAppendKoin
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.registerEvents
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

class ChattyPlugin : JavaPlugin() {
    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()
        ChattyConfig.load()

        ChattyCommands()

        registerEvents(
            ChatListener(),
            PlayerListener()
        )

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
        }
    }
}
