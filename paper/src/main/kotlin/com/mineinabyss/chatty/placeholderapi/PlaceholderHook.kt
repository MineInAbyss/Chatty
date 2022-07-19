package com.mineinabyss.chatty.placeholderapi

import com.mineinabyss.chatty.helpers.chattyPlaceholders
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class PlaceholderHook : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "chatty"
    }

    override fun getAuthor(): String {
        return "boy0000"
    }

    override fun getVersion(): String {
        return "0.1"
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onPlaceholderRequest(player: Player, string: String): String {
        chattyPlaceholders(player, string).forEach { placeholder ->
            if (string == placeholder.key) {
                return placeholder.value
            }
        }
        return string
    }
}
