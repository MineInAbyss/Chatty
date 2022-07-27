package com.mineinabyss.chatty

import com.mineinabyss.chatty.helpers.chattyConfig
import com.mineinabyss.chatty.helpers.protocolManager
import com.mineinabyss.chatty.listeners.*
import com.mineinabyss.chatty.placeholderapi.PlaceholderHook
import com.mineinabyss.geary.addon.autoscan
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.registerEvents
import github.scarsz.discordsrv.DiscordSRV
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.Path


class ChattyPlugin : JavaPlugin() {
    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        saveDefaultEmoteFixer()
        saveDefaultMessages()
        saveDefaultConfig()
        reloadConfig()
        ChattyConfig.load()

        ChattyCommands()

        registerEvents(
            ChatListener(),
            PlayerListener()
        )

        // Register the proxy listener
        server.messenger.registerIncomingPluginChannel(this, chattyProxyChannel, ChattyProxyListener())
        server.messenger.registerOutgoingPluginChannel(this, chattyProxyChannel)

        if (ChattyContext.isPlaceholderApiLoaded)
            PlaceholderHook().register()

        if (ChattyContext.isDiscordSRVLoaded)
            DiscordSRV.api.subscribe(DiscordListener())

        if (chattyConfig.chat.chatPreview.enabled && ChattyContext.isProtocolLibLoaded)
            protocolManager.addPacketListener(ChatPreviewPacketAdapter())

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
        }
    }

    override fun onDisable() {
        if (ChattyContext.isDiscordSRVLoaded)
            DiscordSRV.api.unsubscribe(DiscordListener())

        if (ChattyContext.isPlaceholderApiLoaded)
            PlaceholderHook().unregister()

        if (ChatPreviewPacketAdapter() in protocolManager.packetListeners && ChattyContext.isProtocolLibLoaded)
            protocolManager.removePacketListener(ChatPreviewPacketAdapter())
    }
}

private fun saveDefaultMessages() {
    if (!Path(chatty.dataFolder.path + "/messages.yml").toFile().exists()) {
        chatty.saveResource("messages.yml", false)
    }
}


private fun saveDefaultEmoteFixer() {
    if (!Path(chatty.dataFolder.path + "/emotefixer.yml").toFile().exists()) {
        chatty.saveResource("emotefixer.yml", false)
    }
}
