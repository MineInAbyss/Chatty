package com.mineinabyss.chatty

import com.mineinabyss.chatty.helpers.DiscordEmoteFixer
import com.mineinabyss.chatty.listeners.ChatListener
import com.mineinabyss.chatty.listeners.ChattyProxyListener
import com.mineinabyss.chatty.listeners.DiscordListener
import com.mineinabyss.chatty.listeners.PlayerListener
import com.mineinabyss.chatty.placeholders.PlaceholderAPIHook
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.listeners
import github.scarsz.discordsrv.DiscordSRV
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class ChattyPlugin : JavaPlugin() {
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        val chattyContext = object : ChattyContext {
            override val plugin: ChattyPlugin = this@ChattyPlugin
            override val config: ChattyConfig by config("config") { fromPluginPath(loadDefault = true) }
            override val messages: ChattyMessages by config("messages") { fromPluginPath(loadDefault = true) }
            override val emotefixer: DiscordEmoteFixer by config("emotefixer") { fromPluginPath(loadDefault = true) }
            override val isPlaceholderApiLoaded: Boolean = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
            override val isDiscordSRVLoaded: Boolean = Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")
        }

        DI.add<ChattyContext>(chattyContext)

        saveDefaultAssets()

        // Register the proxy listener
        registerProxyChannels()

        ChattyCommands()
        listeners(ChatListener(), PlayerListener())

        if (chatty.isPlaceholderApiLoaded)
            PlaceholderAPIHook().register()

        if (chatty.isDiscordSRVLoaded)
            DiscordSRV.api.subscribe(DiscordListener())

        /*geary {
            *//*autoscan(classLoader, "com.mineinabyss.chatty") {
                all()
            }*//*
            ChattyCommands()
            listeners(ChatListener(), PlayerListener())
        }*/
    }

    override fun onDisable() {
        if (chatty.isDiscordSRVLoaded)
            DiscordSRV.api.unsubscribe(DiscordListener())
    }

    private fun saveDefaultAssets() {
        chatty.plugin.saveResource("assets/minecraft/font/chatty_heads.json", true)
        chatty.plugin.saveResource("assets/space/textures/ui/utils/null.png", true)
        chatty.plugin.saveResource("assets/space/textures/ui/utils/whiteblank_4.png", true)
    }

    private fun registerProxyChannels() {
        try {
            server.messenger.registerIncomingPluginChannel(this, chattyProxyChannel, ChattyProxyListener())
            server.messenger.registerOutgoingPluginChannel(this, chattyProxyChannel)
        } catch (e: IllegalArgumentException) {
            logger.warning("Could not register proxy channel. Is another plugin using it?")
        }
    }
}
