package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.chattyProxyChannel
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.helpers.getChannelFromId
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

class ChattyProxyListener : PluginMessageListener {
    override fun onPluginMessageReceived(ch: String, player: Player, message: ByteArray) {
        if (ch != chattyProxyChannel) return
        // Get channel from last part of string as it is sent via the proxy message
        val decoded = message.decodeToString()
        val channelId = decoded.substringBefore(" ")
        val channel = getChannelFromId(channelId) ?: return
        val msg = decoded.replaceFirst("$channelId ", "")
        when (channel.channelType) {
            ChannelType.GLOBAL -> Bukkit.getOnlinePlayers()
            ChannelType.RADIUS -> emptyList()
            ChannelType.PERMISSION -> Bukkit.getOnlinePlayers().filter { it.hasPermission(channel.permission) }
            ChannelType.PRIVATE -> emptyList() //TODO Implement this when PRIVATE is more clear
        }.forEach { it.sendMessage(msg.miniMsg()) }
    }
}
