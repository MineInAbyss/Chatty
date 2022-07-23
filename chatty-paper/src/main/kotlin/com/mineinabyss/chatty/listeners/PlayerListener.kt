package com.mineinabyss.chatty.listeners

import com.mineinabyss.chatty.components.HideJoinLeave
import com.mineinabyss.chatty.components.PlayerData
import com.mineinabyss.chatty.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerEditBookEvent
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
//                player.sendPluginMessage(chatty, chattyProxyChannel, PlaceholderAPI.setPlaceholders(player, messages.joinLeave.joinMessage).toByteArray())
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.verifyPlayerChannel()
        if (player.toGeary().has<HideJoinLeave>()) return
        if (chattyConfig.join.enabled)
            joinMessage(translatePlaceholders(player, chattyMessages.joinLeave.joinMessage))
//        if (chattyConfig.join.sendAcrossProxy)
//            player.sendPluginMessage(chatty, chattyProxyChannel, PlaceholderAPI.setPlaceholders(player, messages.proxies.proxyJoin).toByteArray())
    }

    @EventHandler
    fun PlayerQuitEvent.onDisconnect() {
        if (player.toGeary().has<HideJoinLeave>() || player.channelIsProxyEnabled()) return
        if (chattyConfig.leave.enabled)
            quitMessage(translatePlaceholders(player, chattyMessages.joinLeave.leaveMessage))
//        if (chattyConfig.leave.sendAcrossProxy)
//            Bukkit.getServer().sendPluginMessage(chatty, chattyProxyChannel, PlaceholderAPI.setPlaceholders(player, messages.proxies.proxyLeave).toByteArray())
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerEditBookEvent.onBookSign() {
        val meta = newBookMeta
        val config = chattyConfig.book
        if (player.checkPermission(chattyConfig.book.bypassFormatPermission)) {
            if (meta.hasAuthor() && !config.useDisplayNameForAuthor)
                meta.author(newBookMeta.author().fixLegacy())
            else if (meta.hasAuthor() && config.useDisplayNameForAuthor)
                meta.author(player.displayName())
            if (meta.hasTitle())
                meta.title(newBookMeta.title().fixLegacy())
            if (meta.hasPages())
                meta.pages(newBookMeta.pages().map { it.fixLegacy() })
        }
        else {
            if (meta.hasAuthor() && !config.useDisplayNameForAuthor)
                meta.author(newBookMeta.author().serialize().verifyBookStyling().miniMsg())
            else if (meta.hasAuthor() && config.useDisplayNameForAuthor)
                meta.author(player.displayName().serialize().verifyBookStyling().miniMsg())
            if (meta.hasTitle())
                meta.title(newBookMeta.title().serialize().verifyBookStyling().miniMsg())
            if (meta.hasPages())
                meta.pages(newBookMeta.pages().map { it.serialize().verifyBookStyling().miniMsg() })
        }
        newBookMeta = meta
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun SignChangeEvent.onSign() {
        if (player.checkPermission(chattyConfig.sign.bypassFormatPermission)) {
            lines().forEachIndexed { index, line ->
                line(index, line.fixLegacy())
            }
        }
        else {
            lines().forEachIndexed { index, line ->
                line(index, line.serialize().verifySignStyling().miniMsg())
            }
        }
    }
}
