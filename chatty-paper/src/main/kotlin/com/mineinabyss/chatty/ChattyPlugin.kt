package com.mineinabyss.chatty

import com.mineinabyss.chatty.listeners.ChatListener
import com.mineinabyss.chatty.listeners.PlayerListener
import com.mineinabyss.chatty.placeholderapi.PlaceholderHook
import com.mineinabyss.geary.addon.autoscan
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.getService
import com.mineinabyss.idofront.plugin.registerEvents
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener

val chattyPlugin: ChattyPlugin by lazy { JavaPlugin.getPlugin(ChattyPlugin::class.java) }
const val chattyProxyChannel = "chatty:proxy"

interface ChattyContext {
    companion object : ChattyContext by getService()

}

class ChattyPlugin : JavaPlugin(), PluginMessageListener {
    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        server.messenger.registerOutgoingPluginChannel(this, chattyProxyChannel)
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

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
        }
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        server.sendMessage("<newline>".miniMsg())
        server.sendMessage("channel".miniMsg())
        if (channel != chattyProxyChannel) return
        val msg = message.decodeToString().miniMsg()
        msg.broadcastVal("Received pluginmsg: ")
        Bukkit.getOnlinePlayers().forEach {
            it.sendMessage(msg)
        }
    }
}
