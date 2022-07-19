package com.mineinabyss.chatty

import com.mineinabyss.chatty.listeners.ChatListener
import com.mineinabyss.chatty.listeners.ChattyProxyListener
import com.mineinabyss.chatty.listeners.PlayerListener
import com.mineinabyss.chatty.placeholderapi.PlaceholderHook
import com.mineinabyss.geary.addon.autoscan
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.getService
import com.mineinabyss.idofront.plugin.registerEvents
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

val chattyPlugin: ChattyPlugin by lazy { JavaPlugin.getPlugin(ChattyPlugin::class.java) }
const val chattyProxyChannel = "chatty:proxy"

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
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderHook().register()
        }

        registerEvents(
            ChatListener(),
            PlayerListener()
        )

        // Register the proxy listener
        server.messenger.registerIncomingPluginChannel(this, chattyProxyChannel, ChattyProxyListener())
        server.messenger.registerOutgoingPluginChannel(this, chattyProxyChannel)

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
        }
    }
}
