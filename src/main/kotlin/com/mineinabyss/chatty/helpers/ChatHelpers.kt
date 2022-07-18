package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.idofront.messaging.miniMsg
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
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
