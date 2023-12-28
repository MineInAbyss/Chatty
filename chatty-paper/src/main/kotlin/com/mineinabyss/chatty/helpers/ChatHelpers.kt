package com.mineinabyss.chatty.helpers

import com.combimagnetron.imageloader.Avatar
import com.combimagnetron.imageloader.Image.ColorType
import com.combimagnetron.imageloader.ImageUtils
import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.chatty.placeholders.chattyPlaceholderTags
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
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
        Sound.entries.map { it.key.toString() }.toList() else ping.alternativePingSounds

val getPingEnabledChannels: List<String> =
    if ("*" in ping.enabledChannels || "all" in ping.enabledChannels) getAllChannelNames() else ping.enabledChannels

fun String.checkForPlayerPings(channelId: String): Player? {
    val ping = chatty.config.ping
    if (channelId !in getPingEnabledChannels || ping.pingPrefix.isEmpty() || ping.pingPrefix !in this) return null
    val pingedName = this.substringAfter(ping.pingPrefix).substringBefore(" ")
    return Bukkit.getOnlinePlayers().firstOrNull { player ->
        player.name == pingedName || player.chattyNickname?.stripTags() == pingedName
    }
}

fun Component.handlePlayerPings(player: Player, pingedPlayer: Player, pingedChannelData: ChannelData) {
    val ping = chatty.config.ping
    val pingSound = pingedChannelData.pingSound ?: ping.defaultPingSound
    val pingRegex = "${ping.pingPrefix}(${pingedPlayer.chattyNickname}|${pingedPlayer.name})+".toRegex()

    pingRegex.find(this.serialize())?.let { match ->

        val pingMessage = this.replaceText(
            TextReplacementConfig.builder()
                .match(match.value)
                .replacement((ping.pingReceiveFormat +
                ("<insert:@${player.name} ><hover:show_text:'<red>Shift + Click to mention!'>".takeIf { ping.clickToReply } ?: "")
                    + match.value).miniMsg())
                .build()
        )
        if (!pingedChannelData.disablePingSound)
            pingedPlayer.playSound(pingedPlayer.location, pingSound, SoundCategory.VOICE, ping.pingVolume, ping.pingPitch)
        pingedPlayer.sendMessage(pingMessage)

        val pingerMessage = this.replaceText(
            TextReplacementConfig.builder()
                .match(match.value)
                .replacement((ping.pingSendFormat + match.value).miniMsg())
                .build()
        )
        player.sendMessage(pingerMessage)
    }
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

fun String.fixSerializedTags(): String = this.replaceAll("\\\\(?!u)(?!\")(?!:)", "")

fun String.replaceAll(regex: String, replacement: String): String = Pattern.compile(regex).matcher(this).replaceAll(replacement)

fun String.fixLegacy(): Component {
    return if ("§" in this) legacy.deserialize(this)
    else this.fixSerializedTags().miniMsg()
}

fun List<String>.toSentence() = this.joinToString(" ")

fun String.toPlayer() = Bukkit.getPlayer(this)

fun Player.sendFormattedMessage(message: String) =
    this.sendMessage(translatePlaceholders(this, message).parseTags(player, true))

fun Player.sendFormattedMessage(vararg message: String, optionalPlayer: Player? = null) =
    this.sendMessage(
        translatePlaceholders((optionalPlayer ?: this), message.joinToString(" ")).parseTags(
            optionalPlayer ?: this,
            true
        )
    )

fun formattedResult(player: Player, message: Component): Component {
    val channelData = player.toGeary().get<ChannelData>()?.withChannelVerified()
    val channel = channelData?.channel ?: return message
    val parsedFormat = translatePlaceholders(player, channel.format).parseTags(player, true)
    val parsedMessage = Component.text("").color(channel.messageColor).append(message.parseTags(player, false))

    return parsedFormat.append(parsedMessage)
}

fun Component.hoverEventShowText(text: Component) = this.hoverEvent(HoverEventSource.unbox(HoverEvent.showText(text)))

fun formatModerationMessage(messageDeletion: ChattyChannel.MessageDeletion, message: Component, signedMessage: SignedMessage, audience: Audience, source: Player, viewers: Set<Player>): Component {
    fun Component.appendDeletionHover(player: Player): Component {
        return this.hoverEventShowText(chatty.messages.messageDeletion.hoverText.miniMsg())
            .clickEvent(ClickEvent.callback {
                val hoverString = Component.empty().hoverEventShowText(message).serialize()
                if (!signedMessage.canDelete()) return@callback player.sendFormattedMessage(hoverString + chatty.messages.messageDeletion.deletionFailed)

                viewers.forEach {
                    it.deleteMessage(signedMessage)
                    if (player != it && it.hasPermission(ChattyPermissions.MODERATION_PERM))
                        it.sendFormattedMessage(hoverString + chatty.messages.messageDeletion.notifyStaff, optionalPlayer = player)
                }
                player.sendFormattedMessage(hoverString + chatty.messages.messageDeletion.deletionSuccess)
        }).compact()
    }

    return when {
        !messageDeletion.enabled || audience !is Player || audience == source || !audience.hasPermission(ChattyPermissions.MODERATION_PERM) -> message
        messageDeletion.position == ChattyChannel.MessageDeletion.MessageDeletionPosition.PREFIX -> messageDeletion.format.miniMsg().appendDeletionHover(audience).append(message)
        messageDeletion.position == ChattyChannel.MessageDeletion.MessageDeletionPosition.SUFFIX -> message.append(messageDeletion.format.miniMsg().appendDeletionHover(audience))
        else -> message
    }
}

private fun appendPingInsert(matchResult: MatchResult, audience: Player, pingedPlayer: Player, source: Player) : Component {
    return when (audience) {
        pingedPlayer ->
            (ping.pingReceiveFormat + matchResult.value).miniMsg()
                .insertion("@${source.name} ")
                .hoverEventShowText(chatty.messages.ping.replyMessage.miniMsg())
        source -> (ping.pingSendFormat + matchResult.value).miniMsg().style(Style.style(TextDecoration.ITALIC))
        else -> matchResult.value.miniMsg()
    }
}
fun formatPlayerPingMessage(source: Player, pingedPlayer: Player?, audience: Audience, message: Component): Component {
    if (pingedPlayer == null || audience !is Player) return message
    val ping = chatty.config.ping
    val pingRegex = "${ping.pingPrefix}(${pingedPlayer.chattyNickname}|${pingedPlayer.name})+".toRegex()

    return pingRegex.find(message.serialize())?.let { match ->
        message.replaceText(
            TextReplacementConfig.builder()
                .match(match.value)
                .replacement(appendPingInsert(match, audience, pingedPlayer, source))
                .build()
        )
    } ?: message
}