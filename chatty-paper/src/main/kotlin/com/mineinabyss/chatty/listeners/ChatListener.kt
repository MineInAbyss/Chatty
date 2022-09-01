package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.chattyProxyChannel
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.components.CommandSpy
import com.mineinabyss.chatty.components.SpyOnChannels
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.idofront.messaging.serialize
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import kotlin.math.sqrt

class ChatListener : Listener {

    @EventHandler
    fun PlayerCommandPreprocessEvent.onPlayerCommand() {
        Bukkit.getOnlinePlayers().filter { it.toGeary().has<CommandSpy>() }.forEach { p ->
            if (p != player)
                p.sendFormattedMessage(chattyConfig.chat.commandSpyFormat, message, optionalPlayer = player)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatEvent.onPlayerChat() {
        player.verifyPlayerChannel()
        val channelId = player.chattyData.channelId
        val channel = getChannelFromId(channelId) ?: return
        val audiences = viewers()
        val formatted =
            if (player.checkPermission(chattyConfig.chat.bypassFormatPermission)) originalMessage().fixLegacy()
            else originalMessage().serialize().verifyChatStyling().miniMsg()

        if (audiences.isNotEmpty()) audiences.clear()
        audiences.addAll(setAudienceForChannelType(player))
        message("<reset>".miniMsg().append(translatePlaceholders(player, channel.format + formatted.serialize())))

        val pingedPlayer = originalMessage().serialize().checkForPlayerPings(channelId)
        if (pingedPlayer != null && pingedPlayer != player && pingedPlayer in audiences) {
            message().handlePlayerPings(player, pingedPlayer)
            audiences.remove(pingedPlayer)
            audiences.remove(player)
        }

        if (pingedPlayer == null && audiences.isEmpty()) {
            isCancelled = true
            player.sendFormattedMessage(chattyMessages.channels.emptyChannelMessage)
        } else audiences.forEach { audience ->
            RendererExtension().render(player, player.displayName(), message(), audience)
        }

        if (channel.proxy) {
            //Append channel to give proxy info on what channel the message is from
            val proxyMessage = ("${player.name}$ZERO_WIDTH$channelId$ZERO_WIDTH${
                translatePlaceholders(player, channel.format).serialize()
            }$ZERO_WIDTH ".miniMsg().append(message())).serialize().toByteArray()
            player.sendPluginMessage(chatty, chattyProxyChannel, proxyMessage)
        }
        audiences.clear()
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

            ChannelType.PERMISSION -> audiences.addAll(onlinePlayers.filter { p -> p.checkPermission(channel.permission) })
            // Intended for Guilds etc., want to consider finding a non-permission way for this
            ChannelType.PRIVATE -> audiences.add(player)
        }

        audiences.addAll(onlinePlayers.filter { p ->
            p !in audiences && p.toGeary().get<SpyOnChannels>()?.channels?.contains(player.chattyData.channelId) == true
        })

        return audiences
    }

    private fun Player.sendFormattedMessage(vararg message: String, optionalPlayer: Player? = null) =
        this.sendMessage(translatePlaceholders((optionalPlayer ?: this), message.joinToString(" ")))
}

class RendererExtension : ChatRenderer {
    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        if (viewer is Player) {
            viewer.sendMessage(message)
        } else {
            Bukkit.getConsoleSender().sendMessage(message)
        }
        return message
    }
}
