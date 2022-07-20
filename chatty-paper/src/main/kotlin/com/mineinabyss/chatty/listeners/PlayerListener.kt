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
        if (player.toGeary().has<PlayerData>()) return
        if (chattyConfig.join.enabled && chattyConfig.join.firstJoin.enabled) {
            joinMessage(translatePlaceholders(player, chattyMessages.joinLeave.firstJoinMessage))
//            if (chattyConfig.join.firstJoin.enabled)
//                player.sendPluginMessage(chattyPlugin, chattyProxyChannel, PlaceholderAPI.setPlaceholders(player, messages.joinLeave.joinMessage).toByteArray())
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.verifyPlayerChannel()
        if (player.toGeary().has<HideJoinLeave>()) return
        if (chattyConfig.join.enabled)
            joinMessage(translatePlaceholders(player, chattyMessages.joinLeave.joinMessage))
//        if (chattyConfig.join.sendAcrossProxy)
//            player.sendPluginMessage(chattyPlugin, chattyProxyChannel, PlaceholderAPI.setPlaceholders(player, messages.proxies.proxyJoin).toByteArray())
    }

    @EventHandler
    fun PlayerQuitEvent.onDisconnect() {
        if (player.toGeary().has<HideJoinLeave>() || player.channelIsProxyEnabled()) return
        if (chattyConfig.leave.enabled)
            quitMessage(translatePlaceholders(player, chattyMessages.joinLeave.leaveMessage))
//        if (chattyConfig.leave.sendAcrossProxy)
//            Bukkit.getServer().sendPluginMessage(chattyPlugin, chattyProxyChannel, PlaceholderAPI.setPlaceholders(player, messages.proxies.proxyLeave).toByteArray())
    }
}
