package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.chattyProxyChannel
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.helpers.*
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

class ChatListener : Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun AsyncChatEvent.onPlayerChat() {
        player.verifyPlayerChannel()
        val channelId = player.chattyData.channelId
        val channel = getChannelFromId(channelId) ?: return
        val audiences = viewers()
        val formattedMessage =
            if (player.checkPermission(chattyConfig.chat.bypassFormatPermission)) originalMessage().fixLegacy()
            else originalMessage().serialize().verifyChatStyling().miniMsg()

        if (audiences.isNotEmpty()) audiences.clear()
        audiences.addAll(setAudienceForChannelType(player))
        message(
            "<reset>".miniMsg().append(translatePlaceholders(player, channel.format + formattedMessage.serialize()))
        )

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
                translatePlaceholders(
                    player,
                    channel.format
                ).serialize()
            }$ZERO_WIDTH ".miniMsg().append(message())).serialize().toByteArray()
            player.sendPluginMessage(chatty, chattyProxyChannel, proxyMessage)
        }
        audiences.clear()
    }
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
