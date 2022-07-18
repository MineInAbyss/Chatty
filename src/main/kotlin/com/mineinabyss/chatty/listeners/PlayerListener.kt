package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.components.HideJoinLeave
import com.mineinabyss.chatty.components.PlayerData
import com.mineinabyss.chatty.helpers.chattyConfig
import com.mineinabyss.chatty.helpers.translatePlaceholders
import com.mineinabyss.chatty.helpers.verifyPlayerChannel
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.messaging.broadcast
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.onFirstJoin() {
        if (player.toGeary().has<PlayerData>()) return
        if (chattyConfig.join.enabled && chattyConfig.join.firstJoin.enabled) {
            broadcast(chattyConfig.join.firstJoin.message)
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.verifyPlayerChannel()
        if (chattyConfig.join.enabled && !player.toGeary().has<HideJoinLeave>()) {
            joinMessage(translatePlaceholders(player, chattyConfig.join.message))
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onDisconnect() {
        if (chattyConfig.leave.enabled && !player.toGeary().has<HideJoinLeave>()) {
            quitMessage(translatePlaceholders(player, chattyConfig.leave.message))
        }
    }
}
