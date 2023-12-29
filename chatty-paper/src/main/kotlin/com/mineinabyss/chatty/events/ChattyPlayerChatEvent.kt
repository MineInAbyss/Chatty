package com.mineinabyss.chatty.events

import com.mineinabyss.chatty.ChattyChannel
import io.papermc.paper.event.player.AbstractChatEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class ChattyPlayerChatEvent(player: Player, val channel: ChattyChannel, val message: Component, val viewers: MutableSet<Audience>) : PlayerEvent(player, true) {
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList = handlerList
}