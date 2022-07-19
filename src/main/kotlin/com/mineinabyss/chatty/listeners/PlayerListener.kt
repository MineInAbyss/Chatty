package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.components.HideJoinLeave
import com.mineinabyss.chatty.components.PlayerData
import com.mineinabyss.chatty.helpers.chattyConfig
import com.mineinabyss.chatty.helpers.messages
import com.mineinabyss.chatty.helpers.translatePlaceholders
import com.mineinabyss.chatty.helpers.verifyPlayerChannel
import com.mineinabyss.geary.papermc.access.toGeary
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.onFirstJoin() {
        if (player.toGeary().has<PlayerData>()) return
        if (chattyConfig.join.enabled && chattyConfig.join.firstJoin.enabled) {
            joinMessage(translatePlaceholders(player, messages.firstJoinMessage))
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.verifyPlayerChannel()
        if (chattyConfig.join.enabled && !player.toGeary().has<HideJoinLeave>()) {
            joinMessage(translatePlaceholders(player, messages.joinMessage))
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onDisconnect() {
        if (chattyConfig.leave.enabled && !player.toGeary().has<HideJoinLeave>()) {
            quitMessage(translatePlaceholders(player, messages.leaveMessage))
        }
    }
}
