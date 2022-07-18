package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.idofront.messaging.miniMsg
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun String.checkForPlayerPings(channel: ChattyConfig.ChattyChannel): Player? {
    if (channel.ping == null || channel.ping.pingPrefix == "" || channel.ping.pingPrefix !in this) return null
    val pingedName = this.substringAfter(channel.ping.pingPrefix).split(" ")[0]
    return Bukkit.getOnlinePlayers().firstOrNull {
        it.name == pingedName || it.displayName().deserialize() == pingedName
    }
}

fun Component.handlePlayerPings(player: Player, pingedPlayer: Player) {
    val channel = player.playerData.channel
    val ping = channel.ping ?: return
    val displayName = if (channel.format.useDisplayName) pingedPlayer.displayName().stripTags() else pingedPlayer.name
    val clickToReply =
        if (ping.clickToReply) "<insert:@${player.displayName().stripTags()} ><hover:show_text:'<red>Shift + Click to reply!'>"
        else ""
    val pingMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + displayName)
            .replacement((ping.pingFormat + clickToReply + ping.pingPrefix + displayName).miniMsg()).build()
    )
    if (!pingedPlayer.playerData.disablePingSound)
        pingedPlayer.playSound(pingedPlayer.location, ping.pingSound, ping.pingVolume, ping.pingPitch)
    pingedPlayer.sendMessage(pingMessage)
}

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
    val audiences = mutableSetOf<Audience>()

    when (channel.channelType) {
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

fun Component.deserialize() : String {
    return MiniMessage.builder().build().serialize(this)
}

fun Component.stripTags() : String {
    return MiniMessage.builder().build().stripTags(this.deserialize())
}
