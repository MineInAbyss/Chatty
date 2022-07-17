package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.components.HideJoinLeave
import com.mineinabyss.chatty.components.playerData
import com.mineinabyss.chatty.helpers.chattyConfig
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.messaging.broadcast
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        if (player.toGeary().has<HideJoinLeave>()) return
        if (player.playerData.firstJoin) {
            player.playerData.firstJoin = false
            if (chattyConfig.join.enabled && chattyConfig.join.firstJoin.enabled) {
                broadcast(chattyConfig.join.firstJoin.message)
            }
        }
        else if (chattyConfig.join.enabled) {
            broadcast(chattyConfig.join.message)
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onDisconnect() {
        if (chattyConfig.leave.enabled && !player.toGeary().has<HideJoinLeave>()) {
            broadcast(chattyConfig.leave.message)
        }
    }
}
