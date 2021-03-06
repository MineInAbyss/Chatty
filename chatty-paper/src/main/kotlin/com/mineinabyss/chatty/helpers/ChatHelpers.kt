package com.mineinabyss.chatty.helpers

import com.destroystokyo.paper.ClientOption
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.SpyOnLocal
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.miniMsg
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.awt.Color
import javax.imageio.ImageIO

val ZERO_WIDTH = "\u200B"
val ping = chattyConfig.ping
val getAlternativePingSounds: List<String> =
    if ("*" in ping.alternativePingSounds || "all" in ping.alternativePingSounds)
        Sound.values().map { it.key.toString() }.toList() else ping.alternativePingSounds

val getPingEnabledChannels: List<String> =
    if ("*" in ping.enabledChannels || "all" in ping.enabledChannels) getAllChannelNames() else ping.enabledChannels

fun String.checkForPlayerPings(channelId: String): Player? {
    val ping = chattyConfig.ping
    if (channelId !in getPingEnabledChannels || ping.pingPrefix.isEmpty() || ping.pingPrefix !in this) return null
    val pingedName = this.substringAfter(ping.pingPrefix).split(" ")[0]
    return Bukkit.getOnlinePlayers().firstOrNull {
        it.name == pingedName || it.displayName().toPlainText() == pingedName
    }
}

fun Component.handlePlayerPings(player: Player, pingedPlayer: Player) {
    getChannelFromId(player.chattyData.channelId) ?: return
    val ping = chattyConfig.ping
    val pingSound = pingedPlayer.chattyData.pingSound ?: ping.defaultPingSound
    val clickToReply =
        if (ping.clickToReply) "<insert:@${
            player.displayName().stripTags()
        } ><hover:show_text:'<red>Shift + Click to mention!'>"
        else ""
    val pingMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + player.chattyData.displayName)
            .replacement((ping.pingReceiveFormat + clickToReply + ping.pingPrefix + player.chattyData.displayName).miniMsg()).build()
    )

    if (!pingedPlayer.chattyData.disablePingSound)
        pingedPlayer.playSound(pingedPlayer.location, pingSound, ping.pingVolume, ping.pingPitch)
    pingedPlayer.sendMessage(pingMessage)

    val pingerMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + player.chattyData.displayName)
            .replacement((ping.pingSendFormat + clickToReply + ping.pingPrefix + player.chattyData.displayName).miniMsg()).build()
    )
    player.sendMessage(pingerMessage)
}

fun getGlobalChat() =
    chattyConfig.channels.entries.firstOrNull { it.value.channelType == ChannelType.GLOBAL }

fun getRadiusChannel() =
    chattyConfig.channels.entries.firstOrNull { it.value.channelType == ChannelType.RADIUS }

fun getAdminChannel() =
    chattyConfig.channels.entries.firstOrNull { it.value.isStaffChannel }

fun getDefaultChat() =
    chattyConfig.channels.entries.firstOrNull { it.value.isDefaultChannel }
        ?: getGlobalChat()
        ?: throw IllegalStateException("No Default or Global channel found")

fun Player.swapChannelCommand(channelId: String) {
    val newChannel = getChannelFromId(channelId)

    if (newChannel == null) {
        sendFormattedMessage(chattyMessages.channels.noChannelWithName)
    } else if (!checkPermission(newChannel.permission)) {
        sendFormattedMessage(chattyMessages.channels.missingChannelPermission)
    } else {
        chattyData.channelId = channelId
        sendFormattedMessage(chattyMessages.channels.channelChanged)
    }
}

fun getChannelFromId(channelId: String) =
    chattyConfig.channels.entries.firstOrNull { it.key == channelId }?.value

fun Player.getChannelFromPlayer() =
    chattyConfig.channels.entries.firstOrNull { it.key == this.chattyData.channelId }?.value

fun Player.verifyPlayerChannel() {
    if (chattyData.channelId !in chattyConfig.channels)
        chattyData.channelId = getDefaultChat().key
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
    return PlaceholderAPI.setPlaceholders(player, msg.serialize()).serializeLegacy()
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
    val channel = getChannelFromId(player.chattyData.channelId) ?: return emptySet()
    val audiences = mutableSetOf<Audience>()

    when (channel.channelType) {
        ChannelType.GLOBAL -> {
            audiences.addAll(onlinePlayers)
        }
        ChannelType.RADIUS -> {
            if (channel.channelRadius <= 0) audiences.addAll(onlinePlayers)
            else audiences.addAll(onlinePlayers.filter { p ->
                p.toGeary().has<SpyOnLocal>() ||
                (p.location.distanceSquared(player.location) <= channel.channelRadius)
            })
        }
        ChannelType.PERMISSION -> {
            audiences.addAll(onlinePlayers.filter { p -> p.checkPermission(channel.permission) })
        }
        // Intended for Guilds etc, wanna consider finding a non-permission way for this
        ChannelType.PRIVATE -> {
            audiences.addAll(
                onlinePlayers.filter { p ->
                    p.chattyData.channelId == player.chattyData.channelId
                })
        }
    }
    return audiences
}

fun String.serializeLegacy() = LegacyComponentSerializer.legacy('??').deserialize(this).fixLegacy()

fun Component.fixLegacy() : Component =
    this.serialize().replace("\\<", "<").replace("\\>", ">").miniMsg()

// Splits <color> and <gradient:...> tags and checks if theyre allowed
fun String.verifyChatStyling(): String {
    val finalString = this
    this.getTags().filter { tag -> tag !in chattyConfig.chat.allowedTags }.forEach { tag ->
        finalString.replace(tag.toString().lowercase(), "\\<${tag.toString().lowercase()}")
    }
    return finalString
}

fun String.verifyBookStyling(): String {
    val finalString = this
    this.getTags().filter { tag -> tag !in chattyConfig.book.allowedTags }.forEach { tag ->
        finalString.replace(tag.toString().lowercase(), "\\<${tag.toString().lowercase()}")
    }
    return finalString
}

fun String.verifySignStyling(): String {
    val finalString = this
    this.getTags().filter { tag -> tag !in chattyConfig.sign.allowedTags }.forEach { tag ->
        finalString.replace(tag.toString().lowercase(), "\\<${tag.toString().lowercase()}")
    }
    return finalString
}

fun Component.serialize() = MiniMessage.builder().build().serialize(this)

fun Component.toPlainText() = PlainTextComponentSerializer.builder().build().serialize(this)

fun Component.stripTags() = MiniMessage.builder().build().stripTags(this.serialize())

fun String.getTags(): List<ChattyTags> {
    val tags = mutableListOf<ChattyTags>()
    if (" " in this) tags.add(ChattyTags.SPACES)
    MiniMessage.builder().build().deserializeToTree(this).toString()
        .split("TagNode(",") {").filter { "Node" !in it && it.isNotBlank() }.toList().forEach {
            val tag = it.replace("'", "").replace(",", "")
            when {
                tag in ChatColor.values().toString().lowercase() -> tags.add(ChattyTags.TEXTCOLOR)
                tag.startsWith("gradient") -> tags.add(ChattyTags.GRADIENT)
                tag.startsWith("#") -> tags.add(ChattyTags.HEXCOLOR)
                tag.startsWith("i") || tag.startsWith("italic") -> tags.add(ChattyTags.ITALIC)
                tag.startsWith("b") || tag.startsWith("bold") -> tags.add(ChattyTags.BOLD)
                tag.startsWith("u") || tag.startsWith("underline") -> tags.add(ChattyTags.UNDERLINE)
                tag.startsWith("st") || tag.startsWith("strikethrough") -> tags.add(ChattyTags.STRIKETHROUGH)
                tag.startsWith("obf") || tag.startsWith("obfuscated") -> tags.add(ChattyTags.OBFUSCATED)
                tag.startsWith("click") -> tags.add(ChattyTags.CLICK)
                tag.startsWith("hover") -> tags.add(ChattyTags.HOVER)
                tag.startsWith("insert") -> tags.add(ChattyTags.INSERTION)
                tag.startsWith("rainbow") -> tags.add(ChattyTags.RAINBOW)
                tag.startsWith("transition") -> tags.add(ChattyTags.TRANSITION)
                tag.startsWith("reset") -> tags.add(ChattyTags.RESET)
                tag.startsWith("font") -> tags.add(ChattyTags.FONT)
                tag.startsWith("key") -> tags.add(ChattyTags.KEYBIND)
                tag.startsWith("lang") -> tags.add(ChattyTags.TRANSLATABLE)
            }
    }
    return tags.toList()
}

fun List<String>.toSentence() = this.joinToString(" ")

fun String.toPlayer(): Player? {
    return Bukkit.getPlayer(this)
}

fun Player.sendFormattedMessage(message: String) =
    this.sendMessage(translatePlaceholders(this, message).serialize().miniMsg())

fun Player.sendFormattedMessage(message: String, optionalPlayer: Player? = null) =
    this.sendMessage(translatePlaceholders((optionalPlayer ?: this), message))

fun Player.sendFormattedPrivateMessage(messageFormat: String, message: String, receiver: Player) =
    this.sendMessage((translatePlaceholders(receiver, messageFormat).serialize() + message).miniMsg())

fun List<String>.removeFirstArgumentOfStringList(): String =
    this.filter { it != this.first() }.toSentence()

fun CommandSender.sendConsoleMessage(message: String) = this.sendMessage(message.miniMsg())
