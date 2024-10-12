package com.mineinabyss.chatty.helpers

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.ChattyConfig.Chat.FilterFormat
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.chatty.placeholders.chattyPlaceholderTags
import com.mineinabyss.chatty.tags.ChattyTags
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.regex.Pattern

val alternativePingSounds: List<String> =
    chatty.config.ping.let { ping -> if ("*" in ping.alternativePingSounds || "all" in ping.alternativePingSounds)
        Sound.entries.map { it.key.toString() }.toList() else ping.alternativePingSounds }

val pingEnabledChannels: List<String> =
    chatty.config.ping.let { ping -> if ("*" in ping.enabledChannels || "all" in ping.enabledChannels) channelNames() else ping.enabledChannels }

fun String.checkForPlayerPings(channelId: String): Player? {
    val ping = chatty.config.ping
    if (channelId !in pingEnabledChannels || ping.pingPrefix.isEmpty() || ping.pingPrefix !in this) return null
    val pingedName = this.substringAfter(ping.pingPrefix).substringBefore(" ")
    return Bukkit.getOnlinePlayers().firstOrNull { it.name == pingedName }
}

val emptyMiniMessage = MiniMessage.builder().tags(TagResolver.empty()).build()
val miniMessage = MiniMessage.builder().tags(TagResolver.standard()).build()
val gson = GsonComponentSerializer.gson()

fun Player?.buildTagResolver(ignorePermissions: Boolean = false): TagResolver {
    val tagResolver = TagResolver.builder()

    if (ignorePermissions || this?.hasPermission(ChattyPermissions.BYPASS_TAG_PERM) != false) {
        // custom tags
        tagResolver.resolver(chattyPlaceholderTags)
        tagResolver.resolvers(ChattyPermissions.chatFormattingPerms.values)
        this?.let { ChattyTags.HELD_ITEM(this)?.let { tagResolver.resolver(it) } }
    }
    else {
        tagResolver.resolvers(ChattyPermissions.chatFormattingPerms.filter { hasPermission(it.key) }.values)
        if (hasPermission(ChattyPermissions.HELD_ITEM_RESOLVER)) ChattyTags.HELD_ITEM(this)?.let { tagResolver.resolver(it) }
    }

    return tagResolver.build()
}

//TODO This breaks our custom tags, due to component, Component.text("<chatty_nickname>: hi", NamedTextColor.RED), being serialized to "<red>\<chatty_nickname>: hi"
// Thus when deserializing, even with the tag resolver, it assumes the tag is escaped
fun Component.parseTags(player: Player? = null, ignorePermissions: Boolean = false) : Component {
    val mm = if (ignorePermissions) miniMessage else emptyMiniMessage
    return mm.deserialize(this.serialize().fixSerializedTags(), player.buildTagResolver(ignorePermissions))
}

fun Component.removeTrailingSpaces() = this.replaceText(TextReplacementConfig.builder().match(" +\$").replacement("").build())

fun globalChannel() =
    chatty.config.channels.entries.firstOrNull { it.value.channelType == ChannelType.GLOBAL }

fun radiusChannel() =
    chatty.config.channels.entries.firstOrNull { it.value.channelType == ChannelType.RADIUS }

fun adminChannel() =
    chatty.config.channels.entries.firstOrNull { it.value.isStaffChannel }

fun defaultChannel() =
    chatty.config.channels.entries.firstOrNull { it.value.isDefaultChannel }
        ?: globalChannel()
        ?: throw IllegalStateException("No Default or Global channel found")

fun channelNames() = chatty.config.channels.keys.toList()

fun translatePlaceholders(player: Player?, message: String) = if (chatty.isPlaceholderApiLoaded) PlaceholderAPI.setPlaceholders(player, message) else message

fun String.fixSerializedTags(): String = this.replaceAll("\\\\(?!u)(?!\")(?!:)", "")

fun String.replaceAll(regex: String, replacement: String): String = Pattern.compile(regex).matcher(this).replaceAll(replacement)

fun List<String>.toSentence() = this.joinToString(" ")

fun String.toPlayer() = Bukkit.getPlayer(this)

fun Player.sendFormattedMessage(message: String) =
    this.sendMessage(translatePlaceholders(this, message).miniMsg(buildTagResolver(true)))

fun Player.sendFormattedMessage(vararg message: String, optionalPlayer: Player? = null) =
    this.sendMessage(
        translatePlaceholders((optionalPlayer ?: this), message.joinToString(" ")).miniMsg((optionalPlayer ?: this).buildTagResolver(true))
    )

fun appendChannelFormat(message: Component, player: Player): Component {
    val channelData = player.toGearyOrNull()?.get<ChannelData>() ?: return message
    val channel = channelData.withChannelVerified().channel ?: return message
    return appendChannelFormat(message, player, channel)
}
fun appendChannelFormat(message: Component, player: Player, channel: ChattyChannel): Component {
    val parsedFormat = translatePlaceholders(player, channel.format).miniMsg(player.buildTagResolver(true))
    val parsedMessage = Component.empty().color(channel.messageColor).append(message)

    return Component.textOfChildren(parsedFormat, parsedMessage)
}

fun Component.hoverEventShowText(text: Component) = this.hoverEvent(HoverEventSource.unbox(HoverEvent.showText(text)))

fun formatModerationMessage(messageDeletion: ChattyChannel.MessageDeletion, message: Component, messageHistory: Component, signedMessage: SignedMessage, audience: Audience, source: Player, viewers: Set<Player>): Component {
    fun Component.appendDeletionHover(player: Player): Component {
        return when (chatty.config.chat.disableChatSigning) {
            true -> this.hoverEventShowText(Component.text("Chat-Signing is disabled, messages cannot be deleted.", NamedTextColor.RED))
            false -> this.hoverEventShowText(chatty.messages.messageDeletion.hoverText.miniMsg())
                .clickEvent(ClickEvent.callback {
                    val hoverString = Component.empty().hoverEventShowText(messageHistory).serialize()
                    if (!signedMessage.canDelete()) return@callback player.sendFormattedMessage(hoverString + chatty.messages.messageDeletion.deletionFailed)


                    viewers.forEach {
                        it.deleteMessage(signedMessage)
                        if (player != it && it.hasPermission(ChattyPermissions.MODERATION_PERM))
                            it.sendFormattedMessage(hoverString + chatty.messages.messageDeletion.notifyStaff, optionalPlayer = player)
                    }
                    player.sendFormattedMessage(hoverString + chatty.messages.messageDeletion.deletionSuccess)
                }).compact()
        }
    }

    return when {
        !messageDeletion.enabled || audience !is Player || audience == source || !audience.hasPermission(ChattyPermissions.MODERATION_PERM) -> message
        messageDeletion.position == ChattyChannel.MessageDeletion.MessageDeletionPosition.PREFIX -> Component.textOfChildren(messageDeletion.format.miniMsg().appendDeletionHover(audience), message)
        messageDeletion.position == ChattyChannel.MessageDeletion.MessageDeletionPosition.SUFFIX -> Component.textOfChildren(message, messageDeletion.format.miniMsg().appendDeletionHover(audience))
        else -> message
    }
}

private fun appendPingInsert(matchResult: MatchResult, audience: Player, pingedPlayer: Player, source: Player) : Component {
    val ping = chatty.config.ping
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

fun handleChatFilters(message: Component, player: Player?, audience: Player?) : Component? {
    var finalMessage = message
    val serialized = finalMessage.asFlatTextContent()
    val filterFormat = chatty.config.chat.filterFormat
    if (player?.hasPermission(ChattyPermissions.BYPASS_CHAT_FILTERS_PERM) == true) return finalMessage

    val matchResults = chatty.config.chat.filters.flatMap { filter -> filter.findAll(serialized) }
    val blockedWords = matchResults.joinToString(", ") { it.value }
    matchResults.forEach { match ->
        finalMessage = finalMessage.replaceText(TextReplacementConfig.builder()
            .matchLiteral(match.value)
            .replacement(Component.textOfChildren(
                when (filterFormat) {
                    FilterFormat.STRIKETHROUGH -> Component.text(match.value).style(Style.style(TextDecoration.STRIKETHROUGH))
                    FilterFormat.CENSOR -> Component.text("⁎".repeat(match.value.length))
                    FilterFormat.DELETE -> Component.empty()
                    FilterFormat.BLOCK -> {
                        player?.sendFormattedMessage(chatty.messages.chatFilter.blockMessage, blockedWords)
                        if (audience?.hasPermission(ChattyPermissions.MODERATION_PERM) == true)
                            audience.sendFormattedMessage(chatty.messages.chatFilter.notifyStaff + blockedWords)
                        return null
                    }
                }.takeIf { it != Component.empty() }?.let {
                    if (audience?.hasPermission(ChattyPermissions.MODERATION_PERM) == true)
                        it.hoverEventShowText(Component.text(match.value.toCharArray().joinToString(Space.PLUS_1.unicode)).style(Style.style(TextDecoration.ITALIC)))
                    else it
                } ?: Component.empty()
            ))
            .build()).removeTrailingSpaces()
    }

    // If filterFormat is DELETE and message is empty, aka only containing blocked words
    // Give feedback to player and notify staff
    if (finalMessage == Component.empty() && filterFormat == FilterFormat.DELETE) {
        if (audience == player) player?.sendFormattedMessage(chatty.messages.chatFilter.deleteWordsEmptyMessage)
        else if (audience?.hasPermission(ChattyPermissions.MODERATION_PERM) == true)
            audience.sendFormattedMessage(chatty.messages.chatFilter.notifyStaff, blockedWords, optionalPlayer = player)
    }

    return finalMessage.compact().takeIf { it != Component.empty() }
}

fun handleUrlReplacements(message: Component, player: Player?): Component {
    if (!chatty.config.chat.formatURLs) return message
    var component = message
    component.clickEvent()?.takeIf { it.action() == ClickEvent.Action.OPEN_URL }?.let { clickEvent ->
        val (regex, replacement) = chatty.config.chat.urlReplacements.firstOrNull { it.regex in clickEvent.value() }?.let { it.regex to it.replacement } ?: return@let
        val hoverComponent = Component.text(clickEvent.value()).style(Style.style(TextDecoration.UNDERLINED))
        component = component.replaceText(TextReplacementConfig.builder().match(regex.pattern).replacement(replacement.hoverEventShowText(hoverComponent)).build())
    }

    return component.children(component.children().map { handleUrlReplacements(it, player) })
}

fun Component.asFlatTextContent(): String {
    return buildString {
        append((this@asFlatTextContent as? TextComponent)?.content())
        append(this@asFlatTextContent.children().joinToString("") { it.asFlatTextContent() })
    }
}