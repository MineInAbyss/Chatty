package com.mineinabyss.chatty

import com.mineinabyss.chatty.listeners.ChatListener
import com.mineinabyss.chatty.listeners.PlayerListener
import com.mineinabyss.geary.addon.autoscan
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.getService
import com.mineinabyss.idofront.plugin.registerEvents
import org.bukkit.plugin.java.JavaPlugin

val chattyPlugin: ChattyPlugin by lazy { JavaPlugin.getPlugin(ChattyPlugin::class.java) }

interface ChattyContext {
    companion object : ChattyContext by getService()

}

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
