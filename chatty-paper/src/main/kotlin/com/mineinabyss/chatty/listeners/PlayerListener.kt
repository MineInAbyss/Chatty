package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.HideJoinLeave
import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.messaging.serialize
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerJoinEvent.onFirstJoin() {
        if (player.toGeary().has<ChannelData>()) return
        if (chattyConfig.join.enabled && chattyConfig.join.firstJoin.enabled) {
            joinMessage(translatePlaceholders(player, chattyMessages.joinLeave.firstJoinMessage))
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun PlayerJoinEvent.onJoin() {
        player.translatePlayerHeadComponent()
        player.translateFullPlayerSkinComponent()

        player.verifyPlayerChannel()
        if (player.chattyNickname != null)
            player.displayName(player.chattyNickname)
        if (chattyConfig.join.enabled && !player.toGeary().has<HideJoinLeave>())
            joinMessage(translatePlaceholders(player, chattyMessages.joinLeave.joinMessage))
    }

    @EventHandler
    fun PlayerQuitEvent.onDisconnect() {
        // Remove player incase they switch skins
        playerHeadMapCache -= player
        playerBodyMapCache -= player

        if (player.chattyNickname != null)
            player.displayName(player.chattyNickname)
        if (chattyConfig.leave.enabled && !player.toGeary().has<HideJoinLeave>())
            quitMessage(translatePlaceholders(player, chattyMessages.joinLeave.leaveMessage))
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerEditBookEvent.onBookSign() {
        val meta = newBookMeta

        meta.author(newBookMeta.author().serialize().parseTags(player))
        if (meta.hasTitle())
            meta.title(newBookMeta.title().serialize().parseTags(player))
        if (meta.hasPages())
            meta.pages(newBookMeta.pages().map { it.serialize().parseTags(player) })
        newBookMeta = meta
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun SignChangeEvent.onSign() {
        lines().forEachIndexed { index, line ->
            line(index, line.serialize().parseTags(player))
        }
    }
}
