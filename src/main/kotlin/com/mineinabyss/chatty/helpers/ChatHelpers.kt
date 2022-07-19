package com.mineinabyss.chatty.helpers

import com.destroystokyo.paper.ClientOption
import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.miniMsg
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.awt.Color
import javax.imageio.ImageIO

fun String.checkForPlayerPings(channelId: String): Player? {
    val ping = chattyConfig.ping
    if (channelId !in getPingEnabledChannels || ping.pingPrefix == "" || ping.pingPrefix !in this) return null
    val pingedName = this.substringAfter(ping.pingPrefix).split(" ")[0]
    return Bukkit.getOnlinePlayers().firstOrNull {
        it.name == pingedName || it.displayName().deserialize() == pingedName
    }
}

fun Component.handlePlayerPings(player: Player, pingedPlayer: Player) {
    val channel = getChannelFromId(player.playerData.channelId) ?: return
    val ping = chattyConfig.ping
    val pingSound = pingedPlayer.playerData.pingSound ?: ping.defaultPingSound
    val displayName = if (channel.format.useDisplayName) pingedPlayer.displayName().stripTags() else pingedPlayer.name
    val clickToReply =
        if (ping.clickToReply) "<insert:@${
            player.displayName().stripTags()
        } ><hover:show_text:'<red>Shift + Click to reply!'>"
        else ""
    val pingMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + displayName)
            .replacement((ping.pingReceiveFormat + clickToReply + ping.pingPrefix + displayName).miniMsg()).build()
    )

    if (!pingedPlayer.playerData.disablePingSound)
        pingedPlayer.playSound(pingedPlayer.location, pingSound, ping.pingVolume, ping.pingPitch)
    pingedPlayer.sendMessage(pingMessage)

    val pingerMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + displayName)
            .replacement((ping.pingSendFormat + clickToReply + ping.pingPrefix + displayName).miniMsg()).build()
    )
    player.sendMessage(pingerMessage)
}

fun getGlobalChat(): Map.Entry<String, ChattyConfig.ChattyChannel>? {
    return chattyConfig.channels.entries.firstOrNull { it.value.channelType == ChannelType.GLOBAL }
}

fun getDefaultChat(): Map.Entry<String, ChattyConfig.ChattyChannel> {
    return chattyConfig.channels.entries.firstOrNull { it.value.isDefaultChannel }
        ?: getGlobalChat()
        ?: throw IllegalStateException("No Default or Global channel found")
}

fun getChannelFromId(channelId: String) : ChattyConfig.ChattyChannel? {
    return chattyConfig.channels.entries.firstOrNull { it.key == channelId }?.value
}

fun Player.verifyPlayerChannel() {
    if (playerData.channelId !in chattyConfig.channels)
        playerData.channelId = getDefaultChat().key
}

fun getAllChannelNames(): List<String> {
    val list = mutableListOf<String>()
    chattyConfig.channels.forEach { list.add(it.key) }
    return list
}

fun translatePlaceholders(player: Player, message: String): Component {
    val msg = message.miniMsg().replaceText(
        TextReplacementConfig.builder()
            .match("%chatty_playerhead%")
            .replacement(player.translatePlayerHeadComponent()).build()
    )
    return PlaceholderAPI.setPlaceholders(player, msg.deserialize()).miniMsg()
}

//TODO Convert to using BLHE
fun Player.translatePlayerHeadComponent(): Component {
    val message = Component.text()
    val url = playerProfile.textures.skin ?: return message.build()
    val hasHat = getClientOption(ClientOption.SKIN_PARTS).hasHatsEnabled()
    val c = ImageIO.read(url)
    var pixel = '#'

    (0..9).forEach { x ->
        val component = Component.text("$pixel!").font(Key.key("minecraft:chatty_heads"))
        (0..7).forEach { y ->
            val isTransparent = Color(c.getRGB(40 + y, 8 + x - 2), true).alpha <= 254
            val color = if (isTransparent || !hasHat) c.getRGB(8 + y, 8 + x - 2) else c.getRGB(40 + y, 8 + x - 2)
            message.append(component.color(TextColor.color(color)))
        }

        when (x) {
            1 -> pixel = '#'
            2 -> pixel = '$'
            3 -> pixel = '%'
            4 -> pixel = '&'
            5 -> pixel = '\''
            6 -> pixel = '('
            7 -> pixel = ')'
            8 -> pixel = '*'
        }
        message.append(Space.of(-12).miniMsg())
    }
    return message.append(Space.of(8).miniMsg()).build()
}

fun setAudienceForChannelType(player: Player): Set<Audience> {
    val onlinePlayers = Bukkit.getOnlinePlayers()
    val channel = getChannelFromId(player.playerData.channelId) ?: return emptySet()
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
                    p.playerData.channelId == player.playerData.channelId
                })
        }
    }
    return audiences
}

val ping = chattyConfig.ping
val getAlternativePingSounds: List<String> =
    if ("*" in ping.alternativePingSounds || "all" in ping.alternativePingSounds)
        Sound.values().map { it.key.toString() }.toList() else ping.alternativePingSounds

val getPingEnabledChannels: List<String> =
    if ("*" in ping.enabledChannels || "all" in ping.enabledChannels) getAllChannelNames() else ping.enabledChannels


fun Component.deserialize(): String {
    return MiniMessage.builder().build().serialize(this)
}

fun Component.stripTags(): String {
    return MiniMessage.builder().build().stripTags(this.deserialize())
}

fun Player.sendFormattedMessage(message: String) {
    this.sendMessage(translatePlaceholders(this, message))
}
