package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.chattyPlugin
import com.mineinabyss.chatty.chattyProxyChannel
import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.idofront.messaging.miniMsg
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun AsyncChatEvent.onPlayerChat() {
        player.verifyPlayerChannel()
        val channelId = player.playerData.channelId
        val channel = getChannelFromId(channelId) ?: return
        val displayName = if (channel.format.useDisplayName) player.displayName() else player.name.miniMsg()
        val audiences = viewers()
        audiences.clear()
        audiences.addAll(setAudienceForChannelType(player))

        message("<reset>".miniMsg()
                .append(translatePlaceholders(player, channel.format.prefix))
                .append(displayName)
                .append(translatePlaceholders(player, channel.format.suffix))
                .append(channel.format.messageFormat.miniMsg().append(originalMessage()))
        )

        val pingedPlayer = originalMessage().deserialize().checkForPlayerPings(channelId)
        if (pingedPlayer != null && pingedPlayer != player && pingedPlayer in audiences) {
            message().handlePlayerPings(player, pingedPlayer)
            audiences.remove(pingedPlayer)
            audiences.remove(player)
        }

        if (pingedPlayer == null && audiences.isEmpty()) {
            isCancelled = true
            player.sendFormattedMessage(messages.emptyChannelMessage)
        } else audiences.forEach { audience ->
            RendererExtension().render(player, displayName, message(), audience)
        }

        if (channel.proxy) {
            //Append channel to give proxy info on what channel the message is from
            player.sendPluginMessage(chattyPlugin, chattyProxyChannel, ("$channelId " + message().deserialize()).toByteArray())
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
