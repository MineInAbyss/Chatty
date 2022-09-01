package com.mineinabyss.chatty.helpers

import com.combimagnetron.imageloader.Image
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.idofront.messaging.miniMsg
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
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
            player.displayName().stripTags()
        } ><hover:show_text:'<red>Shift + Click to mention!'>"
        else ""
    val pingMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + player.chattyData.displayName)
            .replacement((ping.pingReceiveFormat + clickToReply + ping.pingPrefix + player.chattyData.displayName).miniMsg())
            .build()
    )

    if (!pingedPlayer.chattyData.disablePingSound)
        pingedPlayer.playSound(pingedPlayer.location, pingSound, ping.pingVolume, ping.pingPitch)
    pingedPlayer.sendMessage(pingMessage)

    val pingerMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + player.chattyData.displayName)
            .replacement((ping.pingSendFormat + clickToReply + ping.pingPrefix + player.chattyData.displayName).miniMsg())
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
    )
    return PlaceholderAPI.setPlaceholders(player, msg.serialize()).serializeLegacy()
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
    return MiniMessage.builder().build().deserialize(image).style(Style.style().font(font).build())
}

private fun convertURLToImageString(
    url: String, ascent: Int = 4, colorType: Image.ColorType = Image.ColorType.MINIMESSAGE
): String {
    return Image.builder().image(url).colorType(colorType).ascent(ascent).build().generate()
}

fun String.serializeLegacy() = LegacyComponentSerializer.legacy('§').deserialize(this).fixLegacy()

fun Component.fixLegacy(): Component =
    this.serialize().replace("\\<", "<").replace("\\>", ">").miniMsg()

// Splits <color> and <gradient:...> tags and checks if they're allowed
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

fun Component.serialize() = MiniMessage.builder().build().serialize(this)

fun Component.toPlainText() = PlainTextComponentSerializer.builder().build().serialize(this)

fun Component.stripTags() = MiniMessage.builder().build().stripTags(this.serialize())

fun String.getTags(): List<ChattyTags> {
    val tags = mutableListOf<ChattyTags>()
    if (" " in this) tags.add(ChattyTags.SPACES)
    MiniMessage.builder().build().deserializeToTree(this).toString()
        .split("TagNode(", ") {").filter { "Node" !in it && it.isNotBlank() }.toList().forEach {
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
