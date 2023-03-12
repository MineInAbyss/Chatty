package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.chattyConfig
import com.mineinabyss.chatty.chattyMessages
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.HideJoinLeave
import com.mineinabyss.chatty.helpers.parseTags
import com.mineinabyss.chatty.helpers.refreshSkinInCaches
import com.mineinabyss.chatty.helpers.translatePlaceholders
import com.mineinabyss.chatty.helpers.verifyPlayerChannel
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.textcomponents.serialize
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
        player.verifyPlayerChannel()
        if (chattyConfig.join.enabled && !player.toGeary().has<HideJoinLeave>())
            joinMessage(translatePlaceholders(player, chattyMessages.joinLeave.joinMessage))
    }

    @EventHandler
    fun PlayerQuitEvent.onDisconnect() {
        // Remove player incase they switch skins
        player.refreshSkinInCaches()

        if (chattyConfig.leave.enabled && !player.toGeary().has<HideJoinLeave>())
            quitMessage(translatePlaceholders(player, chattyMessages.joinLeave.leaveMessage))
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerEditBookEvent.onBookSign() {
        newBookMeta = newBookMeta.apply {
            if (hasAuthor())
                author(author().parseTags(player))
            if (hasTitle())
                title(title().parseTags(player))
            if (hasPages())
                pages(pages().map { it.parseTags(player) })
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun SignChangeEvent.onSign() {
        lines().forEachIndexed { index, line ->
            line(index, line.serialize().parseTags(player))
        }
    }
}
