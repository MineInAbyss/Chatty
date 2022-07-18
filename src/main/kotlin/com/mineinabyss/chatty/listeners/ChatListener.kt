package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.chatty.helpers.setAudienceForChannelType
import com.mineinabyss.chatty.helpers.translatePlaceholders
import com.mineinabyss.chatty.helpers.verifyPlayerChannel
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun AsyncChatEvent.onPlayerChat() {
        player.verifyPlayerChannel()
        val channel = player.playerData.channel
        val displayName = if (channel.format.useDisplayName) player.displayName() else player.name.miniMsg()
        val audiences = viewers()
        audiences.clear()
        audiences.addAll(setAudienceForChannelType(player))

        message(
            translatePlaceholders(player, channel.format.prefix)
                .append(player.displayName())
                .append(translatePlaceholders(player, channel.format.suffix))
                .append(channel.format.messageFormat.miniMsg().append(originalMessage()))
        )

        if (audiences.isEmpty() && channel.emptyChannelMessage != null) {
            isCancelled = true
            player.sendMessage(channel.emptyChannelMessage.miniMsg())
        }
        else audiences.forEach { audience ->
            RendererExtension().render(player, displayName, message(), audience)
        }
        audiences.clear()
    }
}

private class RendererExtension : ChatRenderer {
    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        if (viewer is Player) {
            viewer.sendMessage(message)
        } else {
            Bukkit.getConsoleSender().sendMessage(message)
        }
        return message
    }
}
