package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.components.HideJoinLeave
import com.mineinabyss.chatty.helpers.buildTagResolver
import com.mineinabyss.chatty.helpers.parseTags
import com.mineinabyss.chatty.helpers.translatePlaceholders
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
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
        if (chatty.config.join.firstJoin.enabled) {
            val message = chatty.messages.joinLeave.firstJoinMessage
            if (message.isEmpty()) joinMessage(null)
            else joinMessage(translatePlaceholders(player, message).takeUnless { it.isEmpty() }?.miniMsg(player.buildTagResolver(true)))
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun PlayerJoinEvent.onJoin() {
        val gearyPlayer = player.toGeary()
        gearyPlayer.getOrSetPersisting<ChannelData> { ChannelData() }
        if (chatty.config.join.enabled) {
            val message = chatty.messages.joinLeave.joinMessage
            if (message.isEmpty() || gearyPlayer.has<HideJoinLeave>()) joinMessage(null)
            else joinMessage(translatePlaceholders(player, message).takeUnless { it.isEmpty() }?.miniMsg(player.buildTagResolver(true)))
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onDisconnect() {
        if (chatty.config.leave.enabled) {
            val message = chatty.messages.joinLeave.leaveMessage
            if (message.isEmpty() || player.toGeary().has<HideJoinLeave>()) quitMessage(null)
            else quitMessage(translatePlaceholders(player, chatty.messages.joinLeave.leaveMessage).takeUnless { it.isEmpty() }?.miniMsg(player.buildTagResolver(true)))
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerEditBookEvent.onBookSign() {
        if (isSigning) newBookMeta = newBookMeta.apply {
            if (hasAuthor()) author(author().parseTags(player))
            if (hasTitle()) title(title().parseTags(player))
            if (hasPages()) pages(pages().map { it.parseTags(player) })
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun SignChangeEvent.onSign() {
        lines().forEachIndexed { index, line ->
            line(index, line.parseTags(player))
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PrepareAnvilEvent.onAnvilRename() {
        val player = viewers.firstOrNull() as? Player ?: return
        val customName = result?.getData(DataComponentTypes.CUSTOM_NAME) ?: return
        result?.setData(DataComponentTypes.CUSTOM_NAME, customName.parseTags(player))
    }
}
