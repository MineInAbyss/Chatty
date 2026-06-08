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
import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.DIContext
import com.mineinabyss.dependencies.getLazy
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.gearyWorld
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.features.singlePluginLogger
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.plugin.listeners
import github.scarsz.discordsrv.DiscordSRV
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class ChattyPlugin : JavaPlugin(), ChattyContext, DI {
    override val di: DIContext = DI {
        singlePluginLogger(this@ChattyPlugin)
        single<Plugin> { this@ChattyPlugin }
        singleConfig<ChattyConfig>("config.yml") {
            default = ChattyConfig()
        }
        singleConfig<ChattyMessages>("messages.yml") {
            default = ChattyMessages()
        }
        singleConfig<DiscordEmoteFixer>("emotefixer.yml") {
            default = DiscordEmoteFixer()
        }
    }

    override val config: ChattyConfig by di.getLazy()
    override val messages: ChattyMessages by di.getLazy()
    override val emotefixer: DiscordEmoteFixer by di.getLazy()
    override val isPlaceholderApiLoaded: Boolean get() = Plugins.isEnabled("PlaceholderAPI")
    override val isDiscordSRVLoaded: Boolean get() = Plugins.isEnabled("DiscordSRV")
    override val spyingPlayers: CachedQuery<SpyingPlayersQuery> by lazy { gearyPaper.worldManager.global.cache(::SpyingPlayersQuery) }

    override fun onEnable() {
        gearyPaper.configure {
            world.autoscan {
                scan(this@ChattyPlugin.classLoader, listOf("com.mineinabyss.chatty")) {
                    all()
                }
            }
            // register components we'll use async now since they'll error otherwise
            componentId<ChattyNickname>()
            componentId<ChannelData>()
        }
        ChattyContext.instance = this

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

    override fun onDisable() {
        if (chatty.isDiscordSRVLoaded)
            DiscordSRV.api.unsubscribe(DiscordListener())
    }

    private fun saveDefaultAssets() {
        saveResource("assets/minecraft/font/chatty_heads.json", true)
        saveResource("assets/space/textures/ui/utils/null.png", true)
        saveResource("assets/space/textures/ui/utils/whiteblank_4.png", true)
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
