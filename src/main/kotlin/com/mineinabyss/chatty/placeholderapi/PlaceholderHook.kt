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
        return true // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    override fun onPlaceholderRequest(player: Player?, string: String): String? {
        if (player == null) return null
        chattyPlaceholders(player).forEach { placeholder ->
            if (string == placeholder.key) {
                return placeholder.value.toString()
            }
        }
        return string
    }

}
