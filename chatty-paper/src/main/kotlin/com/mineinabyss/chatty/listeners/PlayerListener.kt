package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.components.HideJoinLeave
import com.mineinabyss.chatty.components.PlayerData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {
    //TODO Disable if proxy should handle this
    @EventHandler
    fun PlayerJoinEvent.onFirstJoin() {
        if (player.toGeary().has<PlayerData>() || player.channelIsProxyEnabled()) return
        if (chattyConfig.join.enabled && chattyConfig.join.firstJoin.enabled)
            joinMessage(translatePlaceholders(player, messages.joinLeave.firstJoinMessage))
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.verifyPlayerChannel()
        if (player.toGeary().has<HideJoinLeave>() || player.channelIsProxyEnabled()) return
        if (chattyConfig.join.enabled)
            joinMessage(translatePlaceholders(player, messages.joinLeave.joinMessage))
    }

    @EventHandler
    fun PlayerQuitEvent.onDisconnect() {
        if (player.toGeary().has<HideJoinLeave>() || player.channelIsProxyEnabled()) return
        if (chattyConfig.leave.enabled)
            quitMessage(translatePlaceholders(player, messages.joinLeave.leaveMessage))
    }
}
