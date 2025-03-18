package com.mineinabyss.chatty

import com.mineinabyss.chatty.commands.ChattyCommands
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.ChattyNickname
import com.mineinabyss.chatty.helpers.DiscordEmoteFixer
import com.mineinabyss.chatty.listeners.ChatListener
import com.mineinabyss.chatty.listeners.ChattyProxyListener
import com.mineinabyss.chatty.listeners.DiscordListener
import com.mineinabyss.chatty.listeners.PlayerListener
import com.mineinabyss.chatty.placeholders.PlaceholderAPIHook
import com.mineinabyss.chatty.queries.SpyingPlayersQuery
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.papermc.configure
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.plugin.listeners
import github.scarsz.discordsrv.DiscordSRV
import org.bukkit.plugin.java.JavaPlugin

class ChattyPlugin : JavaPlugin() {

    private val ChattyAddon = createAddon("Chatty", configuration = {
        autoscan(classLoader, "com.mineinabyss.chatty") {
            all()
        }
    })

    override fun onLoad() {
        gearyPaper.configure {
            install(ChattyAddon)
            // register components we'll use async now since they'll error otherwise
            geary.componentId<ChattyNickname>()
            geary.componentId<ChannelData>()
        }
    }

    override fun onEnable() {
        createChattyContext()

        saveDefaultAssets()

        // Register the proxy listener
        registerProxyChannels()
        registerDiscordChannels()

        ChattyCommands.registerCommands()
        ChattyCommands.registerSignedCommands()

        listeners(ChatListener(), PlayerListener())
        if (chatty.isPlaceholderApiLoaded)
            PlaceholderAPIHook().register()

        if (chatty.isDiscordSRVLoaded)
            DiscordSRV.api.subscribe(DiscordListener())
    }

    fun createChattyContext() {
        DI.remove<ChattyContext>()
        val chattyContext = object : ChattyContext {
            override val plugin: ChattyPlugin = this@ChattyPlugin
            override val config: ChattyConfig by config("config", dataFolder.toPath(), ChattyConfig())
            override val messages: ChattyMessages by config("messages", dataFolder.toPath(), ChattyMessages())
            override val emotefixer: DiscordEmoteFixer by config("emotefixer", dataFolder.toPath(), DiscordEmoteFixer())
            override val isPlaceholderApiLoaded: Boolean get() = Plugins.isEnabled("PlaceholderAPI")
            override val isDiscordSRVLoaded: Boolean get() = Plugins.isEnabled("DiscordSRV")
            override val spyingPlayers = gearyPaper.worldManager.global.cache(::SpyingPlayersQuery)
        }

        DI.add<ChattyContext>(chattyContext)
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
        runCatching {
            server.messenger.registerIncomingPluginChannel(this, chattyProxyChannel, ChattyProxyListener())
            server.messenger.registerOutgoingPluginChannel(this, chattyProxyChannel)
        }.onFailure {
            logger.warning("Could not register proxy channel. Is another plugin using it?")
        }
    }

    private fun registerDiscordChannels() {
        runCatching {
            server.messenger.registerOutgoingPluginChannel(this, discordSrvChannel)
        }.onFailure {
            logger.warning("Could not register proxy channel. Is another plugin using it?")
        }
    }
}
