package com.mineinabyss.chatty.helpers

import com.combimagnetron.imageloader.Avatar
import com.combimagnetron.imageloader.Image.ColorType
import com.combimagnetron.imageloader.ImageUtils
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.chatty.placeholders.chattyPlaceholderTags
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.profile.PlayerTextures.SkinModel
import java.util.regex.Pattern

const val ZERO_WIDTH = "\u200B"
val ping = chatty.config.ping
val getAlternativePingSounds: List<String> =
    if ("*" in ping.alternativePingSounds || "all" in ping.alternativePingSounds)
        Sound.values().map { it.key.toString() }.toList() else ping.alternativePingSounds

val getPingEnabledChannels: List<String> =
    if ("*" in ping.enabledChannels || "all" in ping.enabledChannels) getAllChannelNames() else ping.enabledChannels

fun String.checkForPlayerPings(channelId: String): Player? {
    val ping = chatty.config.ping
    if (channelId !in getPingEnabledChannels || ping.pingPrefix.isEmpty() || ping.pingPrefix !in this) return null
    val pingedName = this.substringAfter(ping.pingPrefix).split(" ")[0]
    return Bukkit.getOnlinePlayers().firstOrNull { player ->
        player.name == pingedName || player.chattyNickname?.let {
            MiniMessage.miniMessage().stripTags(it)
        } == pingedName
    }
}

fun Component.handlePlayerPings(player: Player, pingedPlayer: Player, pingedChannelData: ChannelData) {
    val ping = chatty.config.ping
    val pingSound = pingedChannelData.pingSound ?: ping.defaultPingSound
    val clickToReply =
        if (ping.clickToReply) "<insert:@${
            player.chattyNickname?.let { MiniMessage.miniMessage().stripTags(it) }
        } ><hover:show_text:'<red>Shift + Click to mention!'>"
        else ""
    val pingMessage = this.replaceText(
        TextReplacementConfig.builder()
            .match(ping.pingPrefix + pingedPlayer.chattyNickname)
            .replacement((ping.pingReceiveFormat + clickToReply + ping.pingPrefix + pingedPlayer.chattyNickname).miniMsg())
            .build()
    )

    if (!pingedChannelData.disablePingSound)
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

/** Build a unique instance of MiniMessage with an empty TagResolver and deserializes with a generated one that takes permissions into account
 * @param player Format tags based on a player's permission, or null to parse all tags
 * @param ignorePermissions Whether to ignore permissions and parse all tags
 */
fun String.parseTags(player: Player? = null, ignorePermissions: Boolean = false): Component {
    val mm = if (ignorePermissions) MiniMessage.miniMessage() else MiniMessage.builder().tags(TagResolver.empty()).build()
    return mm.deserialize(this.fixSerializedTags(), player.buildTagResolver(ignorePermissions))
}

fun Player?.buildTagResolver(ignorePermissions: Boolean = false): TagResolver {
    val tagResolver = TagResolver.builder()

    if (ignorePermissions || this?.hasPermission(ChattyPermissions.BYPASS_TAG_PERM) != false) {
        // custom tags
        tagResolver.resolver(chattyPlaceholderTags)
        tagResolver.resolvers(ChattyPermissions.chatFormattingPerms.values)
    }
    else tagResolver.resolvers(ChattyPermissions.chatFormattingPerms.filter { hasPermission(it.key) }.map { it.value })

    return tagResolver.build()
}

fun Component.parseTags(player: Player? = null, ignorePermissions: Boolean = false) =
    this.serialize().parseTags(player, ignorePermissions)

fun getGlobalChat() =
    chatty.config.channels.entries.firstOrNull { it.value.channelType == ChannelType.GLOBAL }

fun getRadiusChannel() =
    chatty.config.channels.entries.firstOrNull { it.value.channelType == ChannelType.RADIUS }

fun getAdminChannel() =
    chatty.config.channels.entries.firstOrNull { it.value.isStaffChannel }

fun getDefaultChat() =
    chatty.config.channels.entries.firstOrNull { it.value.isDefaultChannel }
        ?: getGlobalChat()
        ?: throw IllegalStateException("No Default or Global channel found")

// TODO change to data.channel
//fun getChannelFromId(channelId: String) =
//    chatty.config.channels[channelId]

//fun Player.getChannelFromPlayer() =
//    chatty.config.channels.entries.firstOrNull { it.key == this.chattyData.channelId }?.value

fun getAllChannelNames() = chatty.config.channels.keys.toList()

fun translatePlaceholders(player: Player, message: String): Component {
    return PlaceholderAPI.setPlaceholders(player, message).fixLegacy()
}

val playerHeadMapCache = mutableMapOf<OfflinePlayer, Component>()
fun OfflinePlayer.translatePlayerHeadComponent(): Component {
    if (this !in playerHeadMapCache || playerHeadMapCache[this]!!.font() != Key.key(chatty.config.playerHeadFont)) {
        playerHeadMapCache[this] = runCatching { getPlayerHeadTexture(ascent = -5) }.getOrDefault(Component.empty())
    }
    return playerHeadMapCache[this] ?: Component.empty()
}

val playerBodyMapCache = mutableMapOf<OfflinePlayer, Component>()
fun Player.refreshSkinInCaches() {
    playerBodyMapCache -= this
    playerHeadMapCache -= this
}
fun OfflinePlayer.translateFullPlayerSkinComponent(): Component {
    if (this !in playerBodyMapCache || playerBodyMapCache[this]!!.font() != Key.key(chatty.config.playerHeadFont)) {
        playerBodyMapCache[this] = runCatching { getFullPlayerBodyTexture(ascent = -5) }.getOrDefault(Component.empty())
    }
    return playerBodyMapCache[this] ?: Component.empty()
}

fun OfflinePlayer.getPlayerHeadTexture(
    scale: Int = 1,
    ascent: Int = 0,
    colorType: ColorType = ColorType.MINIMESSAGE,
    font: Key = Key.key(chatty.config.playerHeadFont)
): Component {
    val image = avatarBuilder(this, scale, ascent, colorType).getBodyBufferedImage(scale).getSubimage(4, 0, 8, 8)
    return "<font:$font>${ImageUtils.generateStringFromImage(image, colorType, ascent)}</font>".miniMsg()
}

fun OfflinePlayer.getFullPlayerBodyTexture(
    scale: Int = 1,
    ascent: Int = 0,
    colorType: ColorType = ColorType.MINIMESSAGE,
    font: Key = Key.key(chatty.config.playerHeadFont)
): Component {
    val image = avatarBuilder(this, scale, ascent, colorType).getBodyBufferedImage(scale)
    return "<font:$font>${ImageUtils.generateStringFromImage(image, colorType, ascent)}</font>".miniMsg()
}

private fun avatarBuilder(
    player: OfflinePlayer,
    scale: Int = 1,
    ascent: Int = 0,
    colorType: ColorType = ColorType.MINIMESSAGE
): Avatar {
    return Avatar.builder().isSlim(player.playerProfile.apply { this.update() }.textures.skinModel == SkinModel.SLIM)
        .playerName(player.name)
        .ascent(ascent).colorType(colorType).scale(scale).build()
}

fun String.fixSerializedTags(): String = this.replaceAll("\\\\(?!u)(?!\")", "")

fun String.replaceAll(regex: String, replacement: String): String = Pattern.compile(regex).matcher(this).replaceAll(replacement)

fun String.fixLegacy(): Component {
    return if ("§" in this) legacy.deserialize(this)
    else this.fixSerializedTags().miniMsg()
}

fun List<String>.toSentence() = this.joinToString(" ")

fun String.toPlayer() = Bukkit.getPlayer(this)

fun Player.sendFormattedMessage(message: String) =
    this.sendMessage(translatePlaceholders(this, message).parseTags(player, true))

fun formattedResult(player: Player, message: Component): Component {
    val channelData = player.toGeary().get<ChannelData>()?.withChannelVerified()
    val channel = channelData?.channel ?: return message
    val parsedFormat = translatePlaceholders(player, channel.format).parseTags(player, true)
    val parsedMessage = Component.text("").color(channel.messageColor).append(message.parseTags(player, false))

    return parsedFormat.append(parsedMessage)
}
