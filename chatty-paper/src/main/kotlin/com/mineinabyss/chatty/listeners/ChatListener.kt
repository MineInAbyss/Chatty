package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.*
import com.mineinabyss.chatty.components.*
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent
import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import kotlin.math.sqrt

@Suppress("UnstableApiUsage")
class ChatListener : Listener {
    val plainText = PlainTextComponentSerializer.plainText()

    @EventHandler
    fun PlayerCommandPreprocessEvent.onPlayerCommand() {
        Bukkit.getOnlinePlayers().filter { it != player && it.toGeary().has<CommandSpy>() }.forEach { p ->
            p.sendFormattedMessage(chattyConfig.chat.commandSpyFormat, message, optionalPlayer = player)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatCommandDecorateEvent.onCommandPreview() {
        player()?.let { result(originalMessage().parseTags(it, false)) }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatDecorateEvent.onChatPreview() {
        player()?.let { result(formattedResult(it, originalMessage())) }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatEvent.onPlayerChat() {
        player.verifyPlayerChannel()
        val channelId = player.chattyData.channelId
        val channel = getChannelFromId(channelId) ?: return

        if (viewers().isNotEmpty()) viewers().clear()
        viewers() += setAudienceForChannelType(player)

        val pingedPlayer = originalMessage().serialize().checkForPlayerPings(channelId)
        if (pingedPlayer != null && pingedPlayer != player && pingedPlayer in viewers()) {
            message().handlePlayerPings(player, pingedPlayer)
            viewers() -= setOf(pingedPlayer, player)
        }

        if (channel.proxy) {
            //Append channel to give proxy info on what channel the message is from
            val proxyMessage = (("${player.name}$ZERO_WIDTH$channelId$ZERO_WIDTH" +
                    MiniMessage.miniMessage().escapeTags(translatePlaceholders(player, channel.format).serialize()) +
                    ZERO_WIDTH).miniMsg().append(message())).serialize().toByteArray()
            player.sendPluginMessage(chatty, chattyProxyChannel, proxyMessage)
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

        if (pingedPlayer == null && viewers().isEmpty()) {
            player.sendFormattedMessage(chattyMessages.channels.emptyChannelMessage)
            viewers().clear()
        } else if (chattyConfig.chat.disableChatSigning) {
            viewers().forEach { a ->
                RendererExtension.render(player, displayName, message(), a)
            }
            viewers().clear()
        }
    }

    private fun setAudienceForChannelType(player: Player): Set<Audience> {
        val onlinePlayers = Bukkit.getOnlinePlayers()
        val channel = getChannelFromId(player.chattyData.channelId) ?: return emptySet()
        val audiences = mutableSetOf<Audience>()

        when (channel.channelType) {
            ChannelType.GLOBAL -> audiences.addAll(onlinePlayers)
            ChannelType.RADIUS -> {
                if (channel.channelRadius <= 0) audiences.addAll(onlinePlayers)
                else audiences.addAll(onlinePlayers.filter { p ->
                    player.world == p.world && sqrt(player.location.distanceSquared(p.location)) <= channel.channelRadius
                })
            }

            ChannelType.PERMISSION ->
                audiences.addAll(onlinePlayers.filter { p -> p.hasPermission(channel.permission) })
            // Intended for Guilds etc., want to consider finding a non-permission way for this
            ChannelType.PRIVATE -> audiences.add(player)
        }

        audiences.addAll(onlinePlayers.filter { p ->
            p !in audiences && p.toGeary().get<SpyOnChannels>()?.channels?.contains(player.chattyData.channelId) == true
        })

        return audiences
    }

    private fun Player.sendFormattedMessage(vararg message: String, optionalPlayer: Player? = null) =
        this.sendMessage(translatePlaceholders((optionalPlayer ?: this), message.joinToString(" ")).parseTags(optionalPlayer ?: this, true))

    private fun Component.stripMessageFormat(player: Player, channel: ChattyConfig.ChattyChannel) =
        plainText.serialize(this)
            .replace(plainText.serialize(translatePlaceholders(player, channel.format).parseTags(player, true)), "").miniMsg().parseTags(player, false)
}

object RendererExtension : ChatRenderer {
    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        (viewer as? Player ?: Bukkit.getConsoleSender()).sendMessage(message)
        return message
    }
}
