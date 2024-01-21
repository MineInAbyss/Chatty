package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.chattyProxyChannel
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.ChattyTranslation
import com.mineinabyss.chatty.components.CommandSpy
import com.mineinabyss.chatty.events.ChattyPlayerChatEvent
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.textcomponents.serialize
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent
import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
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
    val commandSpyQuery = geary.cache(CommandSpyQuery())

    class CommandSpyQuery : GearyQuery() {
        val player by get<Player>()
        override fun ensure() = this { has<CommandSpy>() }
    }

    @EventHandler
    fun PlayerCommandPreprocessEvent.onPlayerCommand() {
        commandSpyQuery.map { player }.filter { it != player }.forEach { p ->
            p.sendFormattedMessage(chatty.config.chat.commandSpyFormat, message, optionalPlayer = player)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatCommandDecorateEvent.onCommandPreview() {
        player()?.let { result(result().parseTags(it, false)) }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatDecorateEvent.onChatPreview() {
        player()?.let { player ->
            result(result().parseTags(player, false))
            result(appendChannelFormat(result(), player))
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatEvent.onPlayerChat() {
        val ogChannelData = player.toGearyOrNull()?.get<ChannelData>() ?: return
        val channelData = ogChannelData.withChannelVerified()
        val channelId = channelData.channelId
        val channel = channelData.channel ?: return
        val baseMessage = message().children().lastOrNull() ?: message()

        if (viewers().isNotEmpty()) viewers().clear()
        viewers() += channel.getAudience(player)

        val chattyEvent = ChattyPlayerChatEvent(player, channel, message(), viewers())
        if (chattyEvent.callEvent()) message(chattyEvent.message)
        else viewers().clear()

        val simpleMessage = Component.textOfChildren(
            player.name().style(Style.style(TextDecoration.ITALIC)),
            Component.text(": "),
            baseMessage
        )
        if (channel.logToConsole) Bukkit.getConsoleSender().sendMessage(simpleMessage)
        handleProxyMessage(player, channelId, channel, message(), simpleMessage)

        val pingedPlayer = originalMessage().serialize().checkForPlayerPings(channelId)
        val playerViewers = viewers().mapNotNull { it as? Player }.toSet()
        val playerTranslation = player.toGearyOrNull()?.get<ChattyTranslation>()
        when {
            playerViewers.isEmpty() -> player.sendFormattedMessage(chatty.messages.channels.emptyChannelMessage)
            chatty.config.chat.disableChatSigning -> {
                playerViewers.forEach { audience ->
                    val audienceTranslation = audience.toGearyOrNull()?.get<ChattyTranslation>()
                    var finalMessage = message()
                    finalMessage = handleMessageTranslation(channel, playerTranslation, audienceTranslation, finalMessage, signedMessage())
                    finalMessage = handleChatFilters(finalMessage, player, audience) ?: return@forEach
                    finalMessage = formatPlayerPingMessage(player, pingedPlayer, audience, finalMessage)
                    finalMessage = formatModerationMessage(
                        channel.messageDeletion,
                        finalMessage,
                        simpleMessage,
                        signedMessage(),
                        audience,
                        player,
                        playerViewers
                    )

                    audience.sendMessage(finalMessage)
                }

                viewers().clear()
                //isCancelled = true
            }

            else -> renderer { source, _, message, audience ->
                if (audience !is Player) return@renderer Component.empty()

                val audienceTranslation = audience.toGearyOrNull()?.get<ChattyTranslation>()
                var finalMessage = message
                finalMessage = handleMessageTranslation(channel, playerTranslation, audienceTranslation, finalMessage, signedMessage())
                finalMessage = handleChatFilters(finalMessage, player, audience)
                    ?: return@renderer Component.empty()
                finalMessage = formatPlayerPingMessage(source, pingedPlayer, audience, finalMessage)
                finalMessage = formatModerationMessage(
                    channel.messageDeletion,
                    finalMessage,
                    simpleMessage,
                    signedMessage(),
                    audience,
                    source,
                    playerViewers
                )

                return@renderer finalMessage
            }
        }
    }

    private fun handleProxyMessage(
        player: Player,
        channelId: String,
        channel: ChattyChannel,
        message: Component,
        simpleMessage: Component
    ) {
        if (!channel.proxy) return
        val proxyMessage = Component.textOfChildren(player.name(), Component.text(channelId), message, simpleMessage)
        player.sendPluginMessage(chatty.plugin, chattyProxyChannel, gson.serialize(proxyMessage).toByteArray())
    }
}
