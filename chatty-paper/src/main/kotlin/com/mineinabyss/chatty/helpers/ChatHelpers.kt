package com.mineinabyss.chatty.helpers

import com.combimagnetron.imageloader.Image
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.idofront.messaging.serialize
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.Style
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player

const val ZERO_WIDTH = "\u200B"
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
            player.displayName().toPlainText()
        } ><hover:show_text:'<red>Shift + Click to mention!'>"
        else ""
    val pingMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + pingedPlayer.chattyData.displayName)
            .replacement((ping.pingReceiveFormat + clickToReply + ping.pingPrefix + pingedPlayer.chattyData.displayName).miniMsg())
            .build()
    )

    if (!pingedPlayer.chattyData.disablePingSound)
        pingedPlayer.playSound(pingedPlayer.location, pingSound, SoundCategory.VOICE, ping.pingVolume, ping.pingPitch)
    pingedPlayer.sendMessage(pingMessage)

    val pingerMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + pingedPlayer.chattyData.displayName)
            .replacement((ping.pingSendFormat + ping.pingPrefix + pingedPlayer.chattyData.displayName).miniMsg())
            .build()
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
    ).serialize()
    return PlaceholderAPI.setPlaceholders(player, msg).miniMsg().fixLegacy()
}

val playerHeadMapCache = mutableMapOf<Player, Component>()
fun Player.translatePlayerHeadComponent(): Component {
    if (this !in playerHeadMapCache) {
        val image = convertURLToImageString("https://api.mineatar.io/face/$name?scale=1")
        playerHeadMapCache[this] =
            convertToImageComponent(image, Key.key(chattyConfig.playerHeadFont))
                .append(Component.text("").font(Key.key("minecraft:default")))
    }
    return playerHeadMapCache[this]!!
}

private fun convertToImageComponent(image: String, font: Key): Component {
    return mm.deserialize(image).style(Style.style().font(font).build())
}

private fun convertURLToImageString(
    url: String, ascent: Int = 4, colorType: Image.ColorType = Image.ColorType.MINIMESSAGE
): String {
    return Image.builder().image(url).colorType(colorType).ascent(ascent).build().generate()
}

fun Component.fixLegacy(): Component =
    this.serialize().replace("\\<", "<").replace("\\>", ">").miniMsg()

fun Component.serialize() = mm.serialize(this)

fun Component.toPlainText() = plainText.serialize(this)

// Cache tagmap so as it is static
private var cachedTags = mutableSetOf<String>()
fun String.getTags(): Set<ChattyTags> {
    val tags = mutableSetOf<ChattyTags>()
    val allTags = cachedTags.takeIf { it.isNotEmpty() } ?: run {
        cachedTags += ChattyTags.values().map { t -> "<${t.name.lowercase()}" }
        cachedTags += ChatColor.values().map { c -> "<${c.name.lowercase()}" }
        cachedTags
    }

    allTags.forEach {
        val tag = it.replace("<", "")
        if (tag.isNotBlank() && it in this) {
            if (tag.uppercase() in ChattyTags.values().map { t -> t.name })
                tags += ChattyTags.valueOf(tag.uppercase())
            else if (tag.uppercase() in ChatColor.values().map { c -> c.name })
                tags += ChattyTags.TEXTCOLOR
        }
    }
    return tags
}

fun List<String>.toSentence() = this.joinToString(" ")

fun String.toPlayer(): Player? {
    return Bukkit.getPlayer(this)
}

fun Player.swapChannelCommand(channelId: String) {
    val newChannel = getChannelFromId(channelId)

    if (newChannel == null) {
        sendFormattedMessage(chattyMessages.channels.noChannelWithName)
    } else if (!checkPermission(newChannel.permission)) {
        sendFormattedMessage(chattyMessages.channels.missingChannelPermission)
    } else {
        chattyData.channelId = channelId
        chattyData.lastChannelUsed = channelId
        sendFormattedMessage(chattyMessages.channels.channelChanged)
    }
}

fun Player.sendFormattedMessage(message: String) =
    this.sendMessage(translatePlaceholders(this, message).serialize().miniMsg())


