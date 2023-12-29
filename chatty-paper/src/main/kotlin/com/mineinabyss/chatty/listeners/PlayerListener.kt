package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.HideJoinLeave
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.textcomponents.serialize
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerJoinEvent.onFirstJoin() {
        if (player.toGeary().has<ChannelData>()) return
        if (chatty.config.join.enabled && chatty.config.join.firstJoin.enabled) {
            joinMessage(translatePlaceholders(player, chatty.messages.joinLeave.firstJoinMessage))
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun PlayerJoinEvent.onJoin() {
        val gearyPlayer = player.toGeary()
        gearyPlayer.getOrSetPersisting { ChannelData() }
        if (chatty.config.join.enabled && !gearyPlayer.has<HideJoinLeave>())
            joinMessage(translatePlaceholders(player, chatty.messages.joinLeave.joinMessage))
    }

    @EventHandler
    fun PlayerQuitEvent.onDisconnect() {
        // Remove player incase they switch skins
        player.refreshSkinInCaches()

        if (chatty.config.leave.enabled && !player.toGeary().has<HideJoinLeave>())
            quitMessage(translatePlaceholders(player, chatty.messages.joinLeave.leaveMessage))
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PrepareAnvilEvent.onAnvilRename() {
        result = result?.editItemMeta {
            displayName(displayName()?.parseTags(viewers.firstOrNull() as? Player))
        }
    }
}
