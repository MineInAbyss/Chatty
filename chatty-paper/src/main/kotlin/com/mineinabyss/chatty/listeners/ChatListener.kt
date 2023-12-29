package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.chattyProxyChannel
import com.mineinabyss.chatty.components.*
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent
import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

@Suppress("UnstableApiUsage")
class ChatListener : Listener {
    val plainText = PlainTextComponentSerializer.plainText()
    val commandSpyQuery = CommandSpyQuery()

    class CommandSpyQuery : GearyQuery() {
        val Pointer.player by get<Player>()
        val Pointer.commandSpy by family { has<CommandSpy>() }
    }

    @EventHandler
    fun PlayerCommandPreprocessEvent.onPlayerCommand() {
        commandSpyQuery.run { toList { it.player } }.filter { it != player }.forEach { p ->
            p.sendFormattedMessage(chatty.config.chat.commandSpyFormat, message, optionalPlayer = player)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatCommandDecorateEvent.onCommandPreview() {
        player()?.let { result(originalMessage().parseTags(it, false)) }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatDecorateEvent.onChatPreview() {
        player()?.let { result(formattedResult(it, result())) }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatEvent.onPlayerChat() {
        val ogChannelData = player.toGearyOrNull()?.get<ChannelData>() ?: return
        val channelData = ogChannelData.withChannelVerified()
        val channelId = channelData.channelId
        val channel = channelData.channel ?: return

        if (viewers().isNotEmpty()) viewers().clear()
        viewers() += channel.getAudience(player)

        if (channel.proxy) {
            //Append channel to give proxy info on what channel the message is from
            val proxyMessage = (("${player.name}$ZERO_WIDTH$channelId$ZERO_WIDTH" +
                    MiniMessage.miniMessage().escapeTags(translatePlaceholders(player, channel.format).serialize()) +
                    ZERO_WIDTH).miniMsg().append(message())).serialize().toByteArray()
            player.sendPluginMessage(chatty.plugin, chattyProxyChannel, proxyMessage)
        }

        val displayName = player.chattyNickname?.miniMsg() ?: player.displayName()
        if (channel.logToConsole) {
            if (channel.simpleConsoleMessages)
                Bukkit.getConsoleSender().sendMessage(
                    displayName.append(
                        Component.text(": ").append(message().stripMessageFormat(player, channel))
                    )
                )
            else Bukkit.getConsoleSender().sendMessage(message())
        }

        val pingedPlayer = originalMessage().serialize().checkForPlayerPings(channelId)
        val playerViewers = viewers().filterIsInstance<Player>().toSet()
        when {
            viewers().isEmpty() -> player.sendFormattedMessage(chatty.messages.channels.emptyChannelMessage)
            chatty.config.chat.disableChatSigning -> {
                playerViewers.forEach { receiver ->
                    var finalMessage = formatPlayerPingMessage(player, pingedPlayer, receiver, message())
                    finalMessage = formatModerationMessage(
                        channel.messageDeletion,
                        finalMessage,
                        signedMessage(),
                        receiver,
                        player,
                        playerViewers
                    )
                    receiver.sendMessage(finalMessage)
                }
                viewers().clear()
                isCancelled = true
            }

            else -> renderer { source, _, message, audience ->
                var finalMessage = formatPlayerPingMessage(source, pingedPlayer, audience, message)
                finalMessage = formatModerationMessage(
                    channel.messageDeletion,
                    finalMessage,
                    signedMessage(),
                    audience,
                    source,
                    playerViewers
                )
                return@renderer finalMessage
            }
        }
    }

    private fun Component.stripMessageFormat(player: Player, channel: ChattyChannel) =
        plainText.serialize(this)
            .replace(plainText.serialize(translatePlaceholders(player, channel.format).parseTags(player, true)), "")
            .miniMsg().parseTags(player, false)
}
