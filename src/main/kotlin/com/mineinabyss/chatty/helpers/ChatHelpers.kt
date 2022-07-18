package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.idofront.messaging.miniMsg
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun getGlobalChat(): ChattyConfig.ChattyChannel? {
    return chattyConfig.channels.firstOrNull { it.channelType == ChannelType.GLOBAL }
}

fun getDefaultChat(): ChattyConfig.ChattyChannel {
    return chattyConfig.channels.firstOrNull { it.isDefaultChannel }
        ?: getGlobalChat()
        ?: throw IllegalStateException("No Default or Global channel found")
}

fun Player.verifyPlayerChannel() {
    if (playerData.channel !in chattyConfig.channels)
        playerData.channel = getDefaultChat()
}

fun getAllChannelNames() : List<String> {
    val list = mutableListOf<String>()
    chattyConfig.channels.forEach { list.add(it.channelName) }
    return list
}

fun translatePlaceholders(player: Player, message: String): Component {
    return PlaceholderAPI.setPlaceholders(player, message).miniMsg()
}

fun setAudienceForChannelType(player: Player) : Set<Audience>{
    val onlinePlayers = Bukkit.getOnlinePlayers()
    val channel = player.playerData.channel
    val channelType = channel.channelType
    val audiences = mutableSetOf<Audience>()
    when (channelType) {
        ChannelType.GLOBAL -> {
            audiences.addAll(onlinePlayers)
        }
        ChannelType.RADIUS -> {
            if (channel.channelRadius <= 0) audiences.addAll(onlinePlayers)
            else audiences.addAll(onlinePlayers.filter { p ->
                (p.location.distanceSquared(player.location) <= channel.channelRadius)
            })
        }
        ChannelType.PERMISSION -> {
            audiences.addAll(onlinePlayers.filter { p -> p.hasPermission(channel.permission) })
        }
        // Intended for Guilds etc, wanna consider finding a non-permission way for this
        ChannelType.PRIVATE -> {
            audiences.addAll(
                onlinePlayers.filter { p ->
                    p.playerData.channel == channel
                })
        }
    }
    return audiences
}
