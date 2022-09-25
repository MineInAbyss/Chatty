package com.mineinabyss.chatty.helpers

import com.combimagnetron.imageloader.Avatar
import com.combimagnetron.imageloader.Image.ColorType
import com.combimagnetron.imageloader.ImageUtils
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.idofront.messaging.serialize
import com.mineinabyss.idofront.messaging.stripTags
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.profile.PlayerTextures.SkinModel

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
        it.name == pingedName || it.chattyNickname?.serialize().toString().stripTags() == pingedName
    }
}

fun Component.handlePlayerPings(player: Player, pingedPlayer: Player) {
    getChannelFromId(player.chattyData.channelId) ?: return
    val ping = chattyConfig.ping
    val pingSound = pingedPlayer.chattyData.pingSound ?: ping.defaultPingSound
    val clickToReply =
        if (ping.clickToReply) "<insert:@${
            player.chattyNickname?.serialize().toString().stripTags()
        } ><hover:show_text:'<red>Shift + Click to mention!'>"
        else ""
    val pingMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + pingedPlayer.chattyNickname)
            .replacement((ping.pingReceiveFormat + clickToReply + ping.pingPrefix + pingedPlayer.chattyNickname).miniMsg())
            .build()
    )

    if (!pingedPlayer.chattyData.disablePingSound)
        pingedPlayer.playSound(pingedPlayer.location, pingSound, SoundCategory.VOICE, ping.pingVolume, ping.pingPitch)
    pingedPlayer.sendMessage(pingMessage)

    val pingerMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + pingedPlayer.chattyNickname)
            .replacement((ping.pingSendFormat + ping.pingPrefix + pingedPlayer.chattyNickname).miniMsg())
            .build()
    )
    player.sendMessage(pingerMessage)
}

/** Build a unique instance of MM with specific tagresolvers to format */
fun String.parseTagsInString(player: Player): Component {
    val tagResolver = TagResolver.builder()

    if (player.hasPermission(ChattyPermissions.BYPASS_TAG_PERM))
        tagResolver.resolvers(ChattyPermissions.chatFormattingPerms.values)
    else ChattyPermissions.chatFormattingPerms.forEach { (perm, tag) ->
        if (player.hasPermission(perm))
            tagResolver.resolver(tag)
    }

    return MiniMessage.builder().tags(tagResolver.build()).build().deserialize(this)
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
    if (this !in playerHeadMapCache)
        playerHeadMapCache[this] = getPlayerHeadTexture(ascent = -5).font(Key.key("minecraft:default"))
    return playerHeadMapCache[this]!!
}

fun Player.getPlayerHeadTexture(scale: Int = 1, ascent: Int = 0, colorType: ColorType = ColorType.MINIMESSAGE, font: Key = Key.key(chattyConfig.playerHeadFont)) : Component {
    val image = avatarBuilder(this, scale, ascent, colorType).getBodyBufferedImage(scale).getSubimage(4,0,8,8)
    return ImageUtils.generateStringFromImage(image, colorType, ascent).miniMsg().font(font)
}

fun Player.getFullPlayerBodyTexture(scale: Int = 1, ascent: Int = 0, colorType: ColorType = ColorType.MINIMESSAGE, font: Key = Key.key(chattyConfig.playerHeadFont)) : Component {
    val image = avatarBuilder(this, scale, ascent, colorType).getBodyBufferedImage(scale)
    return ImageUtils.generateStringFromImage(image, colorType, ascent).miniMsg().font(font)
}

private fun avatarBuilder(player: Player, scale: Int = 1, ascent: Int = 0, colorType: ColorType = ColorType.MINIMESSAGE) : Avatar {
    return Avatar.builder().isSlim(player.playerProfile.textures.skinModel == SkinModel.SLIM).playerName(player.name).ascent(ascent).colorType(colorType).scale(scale).build()
}

fun Component.fixLegacy(): Component {
    val string = this.serialize().replace("\\<", "<").replace("\\>", ">")
    return if ("§" in serialize())
        legacy.deserialize(string)
    else string.miniMsg()
}

fun List<String>.toSentence() = this.joinToString(" ")

fun String.toPlayer(): Player? {
    return Bukkit.getPlayer(this)
}

fun Player.swapChannelCommand(channelId: String) {
    val newChannel = getChannelFromId(channelId)
    when {
        newChannel == null ->
            sendFormattedMessage(chattyMessages.channels.noChannelWithName)
        !hasPermission(newChannel.permission) ->
            sendFormattedMessage(chattyMessages.channels.missingChannelPermission)
        else -> {
            chattyData.channelId = channelId
            chattyData.lastChannelUsed = channelId
            sendFormattedMessage(chattyMessages.channels.channelChanged)
        }
    }
}

fun Player.sendFormattedMessage(message: String) =
    this.sendMessage(translatePlaceholders(this, message).serialize().miniMsg())
